package venta_condominio.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import venta_condominio.exception.ValidacionUnidadException;
import venta_condominio.model.Departamento;
import venta_condominio.model.EstadoUnidad;
import venta_condominio.model.Municipio;
import venta_condominio.model.Unidad;

/** KAN-51 y KAN-55: UnidadService y F-12 (unidad no disponible). */
class UnidadServiceTest {

    private CotizacionServiceTest.UnidadesEnMemoria repositorio;
    private UnidadService service;

    @BeforeEach
    void preparar() {
        repositorio = new CotizacionServiceTest.UnidadesEnMemoria();
        service = new UnidadService(repositorio);

        repositorio.guardar(unidad("APT-801", EstadoUnidad.DISPONIBLE, "285000000"));
        repositorio.guardar(unidad("APT-902", EstadoUnidad.RESERVADA, "300000000"));
    }

    private static Unidad unidad(String id, EstadoUnidad estado, String precio) {
        return new Unidad(id, "APARTAMENTO", new BigDecimal(precio), 78, 3, 2, "Prueba",
                Departamento.QUINDIO, new Municipio("Armenia", Departamento.QUINDIO), estado, List.of());
    }

    @Test
    void listaSoloUnidadesDisponibles() {
        List<Unidad> disponibles = service.listarDisponibles();

        assertEquals(1, disponibles.size());
        assertEquals("APT-801", disponibles.get(0).getIdentificador());
    }

    @Test
    void editaUnidadDisponibleSinCambiarSuEstado() {
        Unidad cambios = unidad("APT-801", null, "270000000");

        Unidad actualizada = service.actualizarUnidad("APT-801", cambios);

        assertEquals(new BigDecimal("270000000"), actualizada.getPrecioLista());
        assertEquals(EstadoUnidad.DISPONIBLE, actualizada.getEstado());
    }

    @Test
    void f12NoSeEditaUnidadNoDisponible() {
        Unidad cambios = unidad("APT-902", null, "250000000");

        assertThrows(ValidacionUnidadException.class, () -> service.actualizarUnidad("APT-902", cambios));
        assertEquals(new BigDecimal("300000000"), repositorio.buscarPorIdentificador("APT-902").getPrecioLista());
    }

    @Test
    void f12NoSeEliminaUnidadNoDisponible() {
        assertThrows(ValidacionUnidadException.class, () -> service.eliminarUnidad("APT-902"));
        assertNotNull(repositorio.buscarPorIdentificador("APT-902"));
    }

    @Test
    void rechazaUnidadFueraDelQuindio() {
        assertThrows(ValidacionUnidadException.class, () -> new Unidad("X", "CASA", new BigDecimal("1"), 50, 1, 1, "",
                Departamento.CALDAS, new Municipio("Manizales", Departamento.CALDAS), null, List.of()));
    }
}
