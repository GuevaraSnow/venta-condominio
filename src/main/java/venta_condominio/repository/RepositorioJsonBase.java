package venta_condominio.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import venta_condominio.util.Almacenamiento;

/**
 * PATRÓN TEMPLATE METHOD (comportamiento).
 *
 * Define una sola vez el algoritmo para leer y escribir una colección JSON
 * (cotizaciones.json, reservas.json, recorridos.json...). Cada repositorio
 * concreto solo completa los "pasos variables":
 *   - rutaArchivo(): en qué documento se guarda
 *   - tipo():        a qué clase se convierte cada registro
 *   - idDe(...):     cuál es el identificador de cada registro
 *
 * Así se eliminó el código repetido de CotizacionRepositorioJson,
 * ReservaRepositorioJson y RecorridoRepositorioJson.
 */
public abstract class RepositorioJsonBase<T> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ---------------- pasos que define cada subclase ----------------

    protected abstract String rutaArchivo();

    protected abstract Class<T> tipo();

    protected abstract String idDe(T registro);

    // ---------------- métodos plantilla (algoritmo fijo) ----------------

    public List<T> listar() {

        String contenido = Almacenamiento.leerTexto(rutaArchivo());
        List<T> registros = new ArrayList<>();

        if (contenido == null || contenido.isBlank()) {
            return registros;
        }

        try {
            JsonNode raiz = objectMapper.readTree(contenido);

            if (raiz == null || !raiz.isArray()) {
                return registros;
            }

            int indice = 0;

            // Registro por registro: uno dañado no tumba toda la lista
            for (JsonNode nodo : raiz) {
                try {
                    registros.add(objectMapper.treeToValue(nodo, tipo()));
                } catch (Exception errorRegistro) {
                    System.err.println("Se omitió el registro #" + indice + " de " + rutaArchivo()
                            + ": " + errorRegistro.getMessage());
                }
                indice++;
            }

            return registros;

        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer " + rutaArchivo(), e);
        }
    }

    public void guardar(T registro) {
        List<T> registros = listar();
        registros.add(registro);
        escribir(registros);
    }

    public T buscarPorId(String id) {

        for (T registro : listar()) {
            if (idDe(registro).equals(id)) {
                return registro;
            }
        }

        return null;
    }

    public void actualizar(T registro) {

        List<T> registros = listar();

        for (int i = 0; i < registros.size(); i++) {
            if (idDe(registros.get(i)).equals(idDe(registro))) {
                registros.set(i, registro);
                escribir(registros);
                return;
            }
        }

        registros.add(registro);
        escribir(registros);
    }

    private void escribir(List<T> registros) {
        try {
            Almacenamiento.escribirTexto(rutaArchivo(),
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(registros));
        } catch (Exception e) {
            throw new RuntimeException("No se pudo guardar " + rutaArchivo(), e);
        }
    }
}
