package venta_condominio.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import venta_condominio.exception.ValidacionReservaException;
import venta_condominio.model.*;
import venta_condominio.repository.ReservaRepositorio;

/** KAN-111: reglas R002, R004 y reserva sobre unidad no disponible. */
class ReservaServiceTest {

    private static final String ASESOR = "asesor@demo.test";
    private static final String CLIENTE = "cliente@demo.test";
    private static final String OTRO = "otro@demo.test";

    private CotizacionServiceTest.CotizacionesEnMemoria cotizaciones;
    private CotizacionServiceTest.UnidadesEnMemoria unidades;
    private ReservasEnMemoria reservas;
    private ReservaService service;

    @BeforeEach
    void preparar() {
        cotizaciones = new CotizacionServiceTest.CotizacionesEnMemoria();
        unidades = new CotizacionServiceTest.UnidadesEnMemoria();
        reservas = new ReservasEnMemoria();
        CotizacionServiceTest.UsuariosEnMemoria usuarios = new CotizacionServiceTest.UsuariosEnMemoria();

        usuarios.guardar(new Usuario("1", "Asesor", ASESOR, "x", Rol.ASESOR_COMERCIAL, true, null, true, null));
        usuarios.guardar(new Usuario("2", "Laura", CLIENTE, "x", Rol.CLIENTE, true, null, true, null));
        usuarios.guardar(new Usuario("3", "Otro", OTRO, "x", Rol.CLIENTE, true, null, true, null));

        unidades.guardar(unidad("APT-801", "285000000"));
        unidades.guardar(unidad("APT-101", "175000000"));

        service = new ReservaService(reservas, cotizaciones, unidades, usuarios);
        service.setHoy(() -> LocalDate.of(2026, 10, 16));
    }

    private static Unidad unidad(String id, String precio) {
        return new Unidad(id, "APARTAMENTO", new BigDecimal(precio), 78, 3, 2, "Prueba",
                Departamento.QUINDIO, new Municipio("Armenia", Departamento.QUINDIO), EstadoUnidad.DISPONIBLE, List.of());
    }

    private Cotizacion cotizacion(String id, String unidad, EstadoCotizacion estado) {
        Cotizacion c = new Cotizacion(id, unidad, CLIENTE, "Laura Gómez", ASESOR,
                new BigDecimal("285000000"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("285000000"),
                new BigDecimal("85500000"), new BigDecimal("199500000"), "2026-10-16", "2026-10-31", estado, null, null);
        cotizaciones.guardar(c);
        return c;
    }

    @Test
    void reglaR002CuotaEsUnoPorCientoDelPrecioDeLista() {
        assertEquals(new BigDecimal("6900000"), ReservaService.calcularCuota(new BigDecimal("690000000")));
    }

    @Test
    void reglaR002CuotaMinimaCincoMillones() {
        assertEquals(new BigDecimal("5000000"), ReservaService.calcularCuota(new BigDecimal("175000000")));
    }

    @Test
    void reservaConCotizacionFirmadaYBloqueaLaUnidad() {
        cotizacion("c1", "APT-801", EstadoCotizacion.FIRMADA);

        Reserva reserva = service.reservar(new ReservaRequest("c1", CLIENTE));

        assertEquals(EstadoReserva.PENDIENTE_PAGO, reserva.getEstado());
        assertEquals(new BigDecimal("5000000"), reserva.getCuotaReserva());   // 1 % de 285 M = 2,85 M < mínimo
        assertEquals(EstadoUnidad.RESERVADA, unidades.buscarPorIdentificador("APT-801").getEstado());
    }

    @Test
    void reglaR004NoSeReservaSinFirmar() {
        cotizacion("c1", "APT-801", EstadoCotizacion.GENERADA);

        assertThrows(ValidacionReservaException.class, () -> service.reservar(new ReservaRequest("c1", CLIENTE)));
        assertEquals(EstadoUnidad.DISPONIBLE, unidades.buscarPorIdentificador("APT-801").getEstado());
    }

    @Test
    void noSeReservaUnidadNoDisponible() {
        cotizacion("c1", "APT-801", EstadoCotizacion.FIRMADA);
        unidades.actualizar(unidades.buscarPorIdentificador("APT-801").conEstado(EstadoUnidad.EN_RECORRIDO));

        assertThrows(ValidacionReservaException.class, () -> service.reservar(new ReservaRequest("c1", CLIENTE)));
    }

    @Test
    void otroClienteNoPuedeReservarCotizacionAjena() {
        cotizacion("c1", "APT-801", EstadoCotizacion.FIRMADA);

        assertThrows(ValidacionReservaException.class, () -> service.reservar(new ReservaRequest("c1", OTRO)));
    }

    @Test
    void noSeReservaDosVecesLaMismaCotizacion() {
        cotizacion("c1", "APT-801", EstadoCotizacion.FIRMADA);
        service.reservar(new ReservaRequest("c1", ASESOR));

        assertThrows(ValidacionReservaException.class, () -> service.reservar(new ReservaRequest("c1", ASESOR)));
    }

    @Test
    void cancelarDevuelveLaUnidadADisponible() {
        cotizacion("c1", "APT-801", EstadoCotizacion.FIRMADA);
        Reserva reserva = service.reservar(new ReservaRequest("c1", CLIENTE));

        assertThrows(ValidacionReservaException.class, () -> service.cancelar(reserva.getId(), CLIENTE));

        Reserva cancelada = service.cancelar(reserva.getId(), ASESOR);

        assertEquals(EstadoReserva.CANCELADA, cancelada.getEstado());
        assertEquals(EstadoUnidad.DISPONIBLE, unidades.buscarPorIdentificador("APT-801").getEstado());
    }

    static class ReservasEnMemoria implements ReservaRepositorio {
        private final List<Reserva> datos = new ArrayList<>();
        public void guardar(Reserva r) { datos.add(r); }
        public List<Reserva> listar() { return new ArrayList<>(datos); }
        public Reserva buscarPorId(String id) { return datos.stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null); }
        public void actualizar(Reserva r) { datos.removeIf(x -> x.getId().equals(r.getId())); datos.add(r); }
    }
}
