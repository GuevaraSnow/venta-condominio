package venta_condominio.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import venta_condominio.exception.AccesoDenegadoException;
import venta_condominio.exception.ValidacionRecorridoException;
import venta_condominio.model.*;
import venta_condominio.repository.RecorridoRepositorio;

/** KAN-183: agendar recorrido, unidad no disponible y regla R001. */
class RecorridoServiceTest {

    private static final String ASESOR = "asesor@demo.test";
    private static final String CLIENTE = "cliente@demo.test";
    private static final String SIN_REGISTRO = "nuevo@demo.test";

    private CotizacionServiceTest.UnidadesEnMemoria unidades;
    private CotizacionServiceTest.ClientesEnMemoria clientes;
    private CotizacionServiceTest.UsuariosEnMemoria usuarios;
    private RecorridoService service;

    @BeforeEach
    void preparar() {
        unidades = new CotizacionServiceTest.UnidadesEnMemoria();
        clientes = new CotizacionServiceTest.ClientesEnMemoria();
        usuarios = new CotizacionServiceTest.UsuariosEnMemoria();

        Municipio armenia = new Municipio("Armenia", Departamento.QUINDIO);
        clientes.guardar(new Cliente("Laura", "Gómez", "Restrepo", "1094887654", CLIENTE, "3104567890",
                new BigDecimal("8000000"), new BigDecimal("90000000"), "APARTAMENTO", Departamento.QUINDIO, armenia));

        usuarios.guardar(new Usuario("1", "Asesor", ASESOR, "x", Rol.ASESOR_COMERCIAL, true, null, true, null));
        usuarios.guardar(new Usuario("2", "Laura", CLIENTE, "x", Rol.CLIENTE, true, null, true, null));
        usuarios.guardar(new Usuario("3", "Nuevo", SIN_REGISTRO, "x", Rol.CLIENTE, true, null, true, null));

        unidades.guardar(new Unidad("APT-801", "APARTAMENTO", new BigDecimal("285000000"), 78, 3, 2, "Prueba",
                Departamento.QUINDIO, armenia, EstadoUnidad.DISPONIBLE, List.of()));

        service = new RecorridoService(new RecorridosEnMemoria(), unidades, clientes, usuarios);
        service.setHoy(() -> LocalDate.of(2026, 10, 16));
    }

    private Recorrido agendar(String quien, String fecha, String hora) {
        return service.agendar(new RecorridoRequest("APT-801", CLIENTE, quien, fecha, hora));
    }

    @Test
    void clienteRegistradoAgendaSinBloquearLaUnidad() {
        Recorrido r = agendar(CLIENTE, "2026-10-20", "10:00");

        assertEquals(EstadoRecorrido.AGENDADO, r.getEstado());
        assertEquals(EstadoUnidad.DISPONIBLE, unidades.buscarPorIdentificador("APT-801").getEstado());
    }

    @Test
    void reglaR001ClienteSinRegistroNoAgenda() {
        assertThrows(ValidacionRecorridoException.class,
                () -> service.agendar(new RecorridoRequest("APT-801", null, SIN_REGISTRO, "2026-10-20", "10:00")));
    }

    @Test
    void reglaR001SinRegistroNoVeUnidades() {
        AccesoService acceso = new AccesoService(usuarios, clientes);

        assertThrows(AccesoDenegadoException.class, () -> acceso.exigirPuedeVerUnidades(null));
        assertThrows(AccesoDenegadoException.class, () -> acceso.exigirPuedeVerUnidades(SIN_REGISTRO));
        assertNotNull(acceso.exigirPuedeVerUnidades(CLIENTE));
        assertNotNull(acceso.exigirPuedeVerUnidades(ASESOR));
    }

    @Test
    void validaFechaYHorario() {
        assertThrows(ValidacionRecorridoException.class, () -> agendar(CLIENTE, "2026-10-15", "10:00"));
        assertThrows(ValidacionRecorridoException.class, () -> agendar(CLIENTE, "2026-10-20", "19:00"));
    }

    @Test
    void noDosRecorridosALaMismaHora() {
        agendar(CLIENTE, "2026-10-20", "10:00");

        assertThrows(ValidacionRecorridoException.class, () -> agendar(ASESOR, "2026-10-20", "10:00"));
    }

    @Test
    void iniciarPoneLaUnidadEnRecorridoYFinalizarLaLibera() {
        Recorrido r = agendar(ASESOR, "2026-10-16", "09:00");

        service.iniciar(r.getId(), ASESOR);
        assertEquals(EstadoUnidad.EN_RECORRIDO, unidades.buscarPorIdentificador("APT-801").getEstado());

        Recorrido fin = service.finalizar(r.getId(), ASESOR);
        assertEquals(EstadoRecorrido.FINALIZADO, fin.getEstado());
        assertEquals(EstadoUnidad.DISPONIBLE, unidades.buscarPorIdentificador("APT-801").getEstado());
    }

    @Test
    void elClienteNoPuedeIniciarRecorridos() {
        Recorrido r = agendar(CLIENTE, "2026-10-20", "10:00");

        assertThrows(ValidacionRecorridoException.class, () -> service.iniciar(r.getId(), CLIENTE));
    }

    @Test
    void noSeAgendaUnidadEnRecorrido() {
        Recorrido r = agendar(ASESOR, "2026-10-16", "09:00");
        service.iniciar(r.getId(), ASESOR);

        assertThrows(ValidacionRecorridoException.class, () -> agendar(CLIENTE, "2026-10-21", "11:00"));
    }

    @Test
    void noSeAgendaEnUnaHoraQueYaPaso() {
        service.setAhora(() -> java.time.LocalDateTime.of(2026, 10, 16, 11, 30));

        assertThrows(ValidacionRecorridoException.class, () -> agendar(CLIENTE, "2026-10-16", "10:00"));
        agendar(CLIENTE, "2026-10-16", "12:00");
    }

    @Test
    void elClienteNoTieneDosRecorridosALaMismaHora() {
        unidades.guardar(new Unidad("CASA-12", "CASA", new BigDecimal("450000000"), 120, 3, 3, "Prueba",
                Departamento.QUINDIO, new Municipio("Armenia", Departamento.QUINDIO), EstadoUnidad.DISPONIBLE, List.of()));

        agendar(CLIENTE, "2026-10-20", "10:00");

        assertThrows(ValidacionRecorridoException.class,
                () -> service.agendar(new RecorridoRequest("CASA-12", CLIENTE, CLIENTE, "2026-10-20", "10:00")));
    }

    @Test
    void elClienteNoAgendaDosVecesLaMismaUnidad() {
        agendar(CLIENTE, "2026-10-20", "10:00");

        assertThrows(ValidacionRecorridoException.class, () -> agendar(CLIENTE, "2026-10-22", "15:00"));
    }

    @Test
    void elClienteCancelaSuRecorrido() {
        Recorrido r = agendar(CLIENTE, "2026-10-20", "10:00");

        assertEquals(EstadoRecorrido.CANCELADO, service.cancelar(r.getId(), CLIENTE).getEstado());
    }

    static class RecorridosEnMemoria implements RecorridoRepositorio {
        private final List<Recorrido> datos = new ArrayList<>();
        public void guardar(Recorrido r) { datos.add(r); }
        public List<Recorrido> listar() { return new ArrayList<>(datos); }
        public Recorrido buscarPorId(String id) { return datos.stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null); }
        public void actualizar(Recorrido r) { datos.removeIf(x -> x.getId().equals(r.getId())); datos.add(r); }
    }
}
