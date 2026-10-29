# Diagnóstico técnico y propuesta de refactorización

## 1. Contexto

- **Proyecto:** Venta de Condominio (`venta-condominio-main`).
- **Tecnologías:** Java 21, Spring Boot, Gradle; frontend React + Vite.
- **Arquitectura observada:** aplicación web organizada por capas, con controladores REST, servicios, modelos, repositorios, utilidades y patrones de diseño.
- **Funcionalidad analizada:** creación y gestión de cotizaciones y su relación con la reserva de unidades.
- **Requisitos y código relacionados:** comentarios internos F-03, F-04, F-12, R002 y R004.
- **Alcance:** diagnóstico del código fuente proporcionado. No se suministró una arquitectura formal ni una especificación completa de requisitos.

## 2. Arquitectura real y flujo

### 2.1. Componentes identificados

- `controller/CotizacionController.java`: expone endpoints REST y delega las operaciones al servicio correspondiente.
- `service/CotizacionService.java`: valida solicitudes, roles, cliente, unidad, descuentos y precalificación; persiste información y publica eventos.
- `patrones/CotizacionFactory.java`: calcula importes y construye objetos de cotización.
- `service/ReservaService.java`: valida la cotización firmada y la disponibilidad de la unidad; crea la reserva, actualiza el estado de la unidad y publica un evento.
- `repository/ReservaRepositorio.java` y `repository/UnidadRepositorio.java`: definen los contratos de persistencia.
- `repository/ReservaRepositorioJson.java`, `UnidadRepositorioJson.java` y `RepositorioJsonBase.java`: implementan la persistencia basada en archivos JSON.
- `model/Cotizacion.java`, `Unidad.java` y `Reserva.java`: representan los principales objetos del proceso de negocio.

### 2.2. Flujo de reserva

1. `ReservaService.reservar` valida la solicitud y busca la cotización correspondiente.
2. Comprueba que el usuario sea el cliente asociado a la cotización o un miembro autorizado del equipo comercial.
3. Exige que la cotización esté en estado `FIRMADA` y que no exista una reserva activa para ella.
4. Consulta la unidad y verifica que su estado sea `DISPONIBLE`.
5. Crea y guarda la reserva; posteriormente actualiza el estado de la unidad a `RESERVADA`.
6. Publica el evento `UNIDAD_RESERVADA`.

Este flujo permite identificar las responsabilidades principales y los puntos donde podrían presentarse problemas de consistencia entre los datos de la reserva y el estado de la unidad.

## 3. Hallazgos técnicos

### H-01. Las escrituras de reserva no forman una operación atómica visible

- **Tipo:** riesgo arquitectónico.
- **Ubicación:** `src/main/java/venta_condominio/service/ReservaService.java`, método `reservar`, aproximadamente líneas 118–129 del código original.
- **Evidencia:**

```java
Reserva reserva = fabrica.crear(cotizacion, unidad, quien.getEmail(), hoy.get());
reservaRepositorio.guardar(reserva);
unidadRepositorio.actualizar(unidad.conEstado(EstadoUnidad.RESERVADA));
```

- **Observación:** la reserva se persiste y posteriormente se actualiza el estado de la unidad mediante otra operación. La implementación basada en archivos JSON no muestra que ambas escrituras formen una única transacción atómica.
- **Criterio afectado:** consistencia de los datos, robustez ante fallos y control de concurrencia.
- **Riesgo:** si falla la segunda escritura, podría quedar registrada una reserva mientras la unidad continúa apareciendo como disponible. Además, dos solicitudes concurrentes podrían comprobar la disponibilidad antes de que alguna actualice el estado de la unidad.
- **Propuesta de mejora:** definir una frontera de consistencia compatible con la estrategia de almacenamiento. Con una base de datos transaccional, ambas operaciones podrían formar parte de una misma transacción. Si se mantiene la persistencia mediante JSON, se debe diseñar un mecanismo específico de bloqueo, recuperación y control de concurrencia. Añadir únicamente `@Transactional` no garantiza atomicidad sobre escrituras realizadas directamente en archivos.
- **Verificación propuesta:** diseñar pruebas que simulen un fallo entre las dos escrituras y solicitudes concurrentes de reserva sobre una misma unidad.
- **Prioridad:** alta, debido al posible impacto sobre la consistencia del proceso de negocio.
- **Estado:** pendiente. No se modificó este flujo porque requiere una solución diseñada de acuerdo con la estrategia de persistencia y no solamente un cambio local.

### H-02. Una consulta puede actualizar cotizaciones vencidas

- **Tipo:** oportunidad de mejora y riesgo de diseño.
- **Ubicación:** `CotizacionController.java`, endpoint de listado; `CotizacionService.java`, métodos `listar` y `revisarVencimiento`.
- **Evidencia:** el método de listado invoca la revisión de vencimiento, que puede ejecutar `cotizacionRepositorio.actualizar(...)`.
- **Observación:** una petición de tipo GET puede producir escrituras. El comentario del código indica que marcar las cotizaciones vencidas durante el listado es un comportamiento intencional; por lo tanto, no se clasifica como un defecto funcional confirmado.
- **Criterio afectado:** separación de responsabilidades, predictibilidad de las consultas y facilidad de prueba.
- **Riesgo:** las consultas repetidas pueden producir escrituras innecesarias y el cambio de estado puede depender de que se ejecute el listado.
- **Propuesta de mejora:** evaluar la posibilidad de gestionar el vencimiento mediante un comando explícito o una tarea programada, conservando las reglas temporales actuales y los estados permitidos.
- **Verificación propuesta:** probar las fechas límite y comprobar que las cotizaciones firmadas o rechazadas no cambien indebidamente de estado.
- **Prioridad:** media.
- **Estado:** pendiente de decisión del equipo. El comportamiento existente debe revisarse antes de cambiarlo para evitar alterar una regla de negocio intencional.

### H-03. Método privado sin uso en `CotizacionService`

- **Tipo:** deuda técnica y oportunidad de mejora.
- **Ubicación original:** `src/main/java/venta_condominio/service/CotizacionService.java`, método `nombreCompleto`, aproximadamente líneas 297–300 del código original.
- **Evidencia original:** el método privado construía el nombre completo del cliente, pero la búsqueda de referencias en el código fuente revisado solo encontró su declaración. `CotizacionFactory` realiza una composición del nombre por separado.
- **Criterio afectado:** mantenibilidad, claridad y limpieza del código.
- **Riesgo:** bajo. El método sin uso añade código innecesario y puede confundir a futuros mantenedores.
- **Acción aplicada:** se eliminó el método privado sin uso de `CotizacionService`. No se modificó el cálculo de la cotización ni la composición del nombre que realiza `CotizacionFactory`.
- **Verificación propuesta:** revisar el archivo modificado, buscar referencias al método y ejecutar las pruebas automatizadas.
- **Prioridad:** baja, porque es una modificación localizada y de alcance reducido.
- **Estado:** cambio aplicado. La ejecución de las pruebas automatizadas finalizó con `BUILD SUCCESSFUL`; este resultado no demuestra por sí solo la cobertura de todos los escenarios de cotizaciones.

## 4. Priorización de los hallazgos

Se utiliza una escala de 1 a 3. En impacto y riesgo, 3 representa el nivel más alto. En esfuerzo, 3 representa la intervención más costosa.

| ID | Impacto | Riesgo | Esfuerzo | Justificación |
|---|---:|---:|---:|---|
| H-01 | 3 | 3 | 3 | Puede afectar la consistencia de una reserva y requiere una solución compatible con la persistencia utilizada. |
| H-02 | 2 | 2 | 2 | Una consulta puede realizar escrituras, aunque el comportamiento parece intencional. |
| H-03 | 1 | 1 | 1 | Es una limpieza localizada, de bajo riesgo y alcance pequeño. |

### Intervención seleccionada

Se seleccionó H-03 porque permite realizar una refactorización pequeña y de bajo riesgo, sin necesidad de modificar reglas de negocio ni el flujo principal de las reservas.

H-01 permanece registrado como riesgo arquitectónico pendiente de una solución específica. H-02 queda como oportunidad de mejora que debe evaluarse antes de cambiar el comportamiento existente.

## 5. Intervención de refactorización

### 5.1. Situación anterior

La clase `CotizacionService` contenía un método privado llamado `nombreCompleto(Cliente)` que no tenía llamadas identificadas en el código fuente revisado.

La existencia de este método duplicaba parcialmente una responsabilidad que también se resolvía en `CotizacionFactory`.

### 5.2. Cambio realizado

Se eliminó el método privado sin uso de:

`src/main/java/venta_condominio/service/CotizacionService.java`

Se mantuvo intacta la composición del nombre realizada por `CotizacionFactory`, así como el cálculo de importes y las demás operaciones de cotización.

### 5.3. Alcance del cambio

- Se eliminó código privado que no tenía usos identificados.
- No se modificaron las reglas de negocio.
- No se modificaron los estados de las cotizaciones ni de las unidades.
- No se modificaron los endpoints REST.
- No se modificó la estrategia de persistencia.
- No se intervino el flujo de creación de reservas.

### 5.4. Justificación

La eliminación reduce código innecesario y facilita la lectura y el mantenimiento de la clase. Al tratarse de un método privado sin usos identificados, el alcance del cambio es reducido.

La conservación del comportamiento observable debe respaldarse con la revisión del código y las pruebas pertinentes; la eliminación de un método sin uso identificado no sustituye esa verificación.

## 6. Verificación y limitaciones

### 6.1. Inspección del código

Se identificó el método privado `nombreCompleto(Cliente)` en la versión original de `CotizacionService.java`. La verificación final consiste en confirmar que el método ya no aparece en el archivo modificado y que no quedan referencias que dependan de él.

### 6.2. Ejecución de pruebas automatizadas

Se ejecutó el siguiente comando desde PowerShell, en la carpeta raíz del proyecto:

```powershell
.\gradlew.bat test
```

El resultado obtenido fue:

```text
BUILD SUCCESSFUL in 30s
4 actionable tasks: 4 executed
```

La ejecución finalizó correctamente en 30 segundos y Gradle reportó cuatro tareas ejecutadas.

Este resultado confirma que las tareas ejecutadas terminaron correctamente. Sin embargo, por sí solo no demuestra que existan pruebas para todos los escenarios de negocio, que todas las funcionalidades estén cubiertas o que se haya comprobado exhaustivamente la conservación del comportamiento observable.

### 6.3. Pruebas adicionales recomendadas

Para reforzar la verificación de la intervención y del flujo analizado, se recomienda:

1. Confirmar que el código modificado ya no contiene el método privado eliminado.
2. Revisar las pruebas existentes relacionadas con la creación y consulta de cotizaciones.
3. Probar la creación de cotizaciones con clientes cuyo segundo apellido sea nulo y con clientes que tengan segundo apellido.
4. Verificar que los importes y los datos resultantes de las cotizaciones sigan las reglas de negocio existentes.
5. Diseñar pruebas de integración para los fallos de persistencia entre el guardado de una reserva y la actualización del estado de la unidad.
6. Evaluar pruebas de concurrencia para solicitudes que intenten reservar simultáneamente una misma unidad.

Las pruebas adicionales son recomendaciones; no se afirma que se hayan ejecutado.

## 7. Cierre y deuda técnica pendiente

La intervención realizada elimina un método privado sin uso identificado en `CotizacionService`, con un alcance reducido y sin cambios intencionales en las reglas de negocio. La ejecución de `.\gradlew.bat test` terminó con `BUILD SUCCESSFUL`.

El hallazgo de mayor prioridad continúa siendo la consistencia entre el guardado de una reserva y la actualización del estado de la unidad. Resolverlo requiere definir una estrategia adecuada para la persistencia JSON o considerar una alternativa transaccional, además de diseñar pruebas de fallo y concurrencia.

También se recomienda evaluar el efecto de escritura que ocurre durante el listado de cotizaciones, teniendo en cuenta que el comportamiento está descrito como intencional en el código existente.

Como práctica preventiva, se recomienda combinar revisión de código, pruebas automatizadas, pruebas de integración y validación de concurrencia para las operaciones que modifican varios recursos relacionados.
