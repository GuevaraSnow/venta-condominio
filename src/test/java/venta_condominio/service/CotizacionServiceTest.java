package venta_condominio.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import venta_condominio.exception.ValidacionCotizacionException;
import venta_condominio.model.*;
import venta_condominio.repository.ClienteRepositorio;
import venta_condominio.repository.CotizacionRepositorio;
import venta_condominio.repository.UnidadRepositorio;
import venta_condominio.repository.UsuarioRepositorio;

/** KAN-103: cálculos de la cotización y regla R003. */
class CotizacionServiceTest {

    private static final String ASESOR = "asesor@demo.test";
    private static final String DIRECTOR = "director@demo.test";
    private static final String CLIENTE = "cliente@demo.test";

    private CotizacionesEnMemoria cotizaciones;
    private UnidadesEnMemoria unidades;
    private CotizacionService service;
    private LocalDate hoy;

    @BeforeEach
    void preparar() {
        cotizaciones = new CotizacionesEnMemoria();
        unidades = new UnidadesEnMemoria();

        ClientesEnMemoria clientes = new ClientesEnMemoria();
        UsuariosEnMemoria usuarios = new UsuariosEnMemoria();

        Municipio armenia = new Municipio("Armenia", Departamento.QUINDIO);
        clientes.guardar(new Cliente("Laura", "Gómez", "Restrepo", "1094887654", CLIENTE, "3104567890",
                new BigDecimal("6500000"), new BigDecimal("280000000"), "APARTAMENTO", Departamento.QUINDIO, armenia));

        usuarios.guardar(new Usuario("1", "Asesor", ASESOR, "x", Rol.ASESOR_COMERCIAL, true, null, true, null));
        usuarios.guardar(new Usuario("2", "Director", DIRECTOR, "x", Rol.DIRECTOR_COMERCIAL, true, null, true, null));
        usuarios.guardar(new Usuario("3", "Laura", CLIENTE, "x", Rol.CLIENTE, true, null, true, null));

        unidades.guardar(unidad("APT-801", EstadoUnidad.DISPONIBLE));
        unidades.guardar(unidad("APT-902", EstadoUnidad.RESERVADA));

        service = new CotizacionService(cotizaciones, unidades, clientes, usuarios);
        hoy = LocalDate.of(2026, 10, 16);
        service.setHoy(() -> hoy);
    }

    private static Unidad unidad(String id, EstadoUnidad estado) {
        return new Unidad(id, "APARTAMENTO", new BigDecimal("285000000"), 78, 3, 2, "Prueba",
                Departamento.QUINDIO, new Municipio("Armenia", Departamento.QUINDIO), estado, List.of());
    }

    private Cotizacion crear(String descuento) {
        return service.crear(new CotizacionRequest("APT-801", CLIENTE, ASESOR, new BigDecimal(descuento)));
    }

    @Test
    void calculaPrecioFinalCuotaInicialYSaldo() {
        CotizacionService.Calculo c = CotizacionService.calcular(new BigDecimal("285000000"), new BigDecimal("2"));

        assertEquals(new BigDecimal("5700000"), c.valorDescuento());
        assertEquals(new BigDecimal("279300000"), c.precioFinal());
        assertEquals(new BigDecimal("83790000"), c.cuotaInicial());       // 30 %
        assertEquals(new BigDecimal("195510000"), c.saldoFinanciar());
    }

    @Test
    void descuentoHastaDosPorCientoQuedaGenerada() {
        Cotizacion cotizacion = crear("2");

        assertEquals(EstadoCotizacion.GENERADA, cotizacion.getEstado());
        assertEquals("2026-10-31", cotizacion.getFechaVencimiento());     // 15 días
    }

    @Test
    void reglaR003DescuentoMayorADosPorCientoRequiereAutorizacion() {
        Cotizacion cotizacion = crear("2.5");

        assertEquals(EstadoCotizacion.PENDIENTE_AUTORIZACION, cotizacion.getEstado());
        assertEquals(1, service.listarPendientes().size());
    }

    @Test
    void elDirectorAutorizaYElClienteFirma() {
        Cotizacion pendiente = crear("5");

        Cotizacion autorizada = service.autorizar(pendiente.getId(), new AccionCotizacionRequest(DIRECTOR, null));
        assertEquals(EstadoCotizacion.GENERADA, autorizada.getEstado());
        assertEquals(DIRECTOR, autorizada.getGestionadoPor());

        Cotizacion firmada = service.firmar(pendiente.getId(), new AccionCotizacionRequest(CLIENTE, null));
        assertEquals(EstadoCotizacion.FIRMADA, firmada.getEstado());
    }

    @Test
    void noSePuedeFirmarSinAutorizacion() {
        Cotizacion pendiente = crear("5");

        assertThrows(ValidacionCotizacionException.class,
                () -> service.firmar(pendiente.getId(), new AccionCotizacionRequest(CLIENTE, null)));
    }

    @Test
    void elAsesorNoPuedeAutorizarDescuentos() {
        Cotizacion pendiente = crear("5");

        assertThrows(ValidacionCotizacionException.class,
                () -> service.autorizar(pendiente.getId(), new AccionCotizacionRequest(ASESOR, null)));
    }

    @Test
    void rechazaDescuentoMayorAlMaximo() {
        assertThrows(ValidacionCotizacionException.class, () -> crear("10.5"));
    }

    @Test
    void noCotizaUnidadNoDisponible() {
        assertThrows(ValidacionCotizacionException.class,
                () -> service.crear(new CotizacionRequest("APT-902", CLIENTE, ASESOR, BigDecimal.ZERO)));
    }

    @Test
    void unClienteNoPuedeGenerarCotizaciones() {
        assertThrows(ValidacionCotizacionException.class,
                () -> service.crear(new CotizacionRequest("APT-801", CLIENTE, CLIENTE, BigDecimal.ZERO)));
    }

    @Test
    void marcaVencidaDespuesDeQuinceDias() {
        Cotizacion cotizacion = crear("0");

        hoy = LocalDate.of(2026, 11, 1);

        assertEquals(EstadoCotizacion.VENCIDA, service.listarPorCliente(CLIENTE).get(0).getEstado());
        assertThrows(ValidacionCotizacionException.class,
                () -> service.firmar(cotizacion.getId(), new AccionCotizacionRequest(CLIENTE, null)));
    }

    // ------------------------------------------------ repositorios en memoria

    static class CotizacionesEnMemoria implements CotizacionRepositorio {
        private final List<Cotizacion> datos = new ArrayList<>();
        public void guardar(Cotizacion c) { datos.add(c); }
        public List<Cotizacion> listar() { return new ArrayList<>(datos); }
        public Cotizacion buscarPorId(String id) { return datos.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null); }
        public void actualizar(Cotizacion c) { datos.removeIf(x -> x.getId().equals(c.getId())); datos.add(c); }
    }

    static class UnidadesEnMemoria implements UnidadRepositorio {
        private final List<Unidad> datos = new ArrayList<>();
        public void guardar(Unidad u) { datos.add(u); }
        public List<Unidad> listar() { return new ArrayList<>(datos); }
        public Unidad buscarPorIdentificador(String id) { return datos.stream().filter(u -> u.getIdentificador().equalsIgnoreCase(id)).findFirst().orElse(null); }
        public List<Unidad> listarPorEstado(EstadoUnidad e) { return datos.stream().filter(u -> u.getEstado() == e).toList(); }
        public void actualizar(Unidad u) { eliminar(u.getIdentificador()); datos.add(u); }
        public boolean eliminar(String id) { return datos.removeIf(u -> u.getIdentificador().equalsIgnoreCase(id)); }
    }

    static class ClientesEnMemoria implements ClienteRepositorio {
        private final List<Cliente> datos = new ArrayList<>();
        public void guardar(Cliente c) { datos.add(c); }
        public List<Cliente> listar() { return new ArrayList<>(datos); }
        public Cliente buscarPorEmail(String email) { return datos.stream().filter(c -> c.getEmail().equalsIgnoreCase(email)).findFirst().orElse(null); }
        public void eliminar(String email) { datos.removeIf(c -> c.getEmail().equalsIgnoreCase(email)); }
    }

    static class UsuariosEnMemoria implements UsuarioRepositorio {
        private final List<Usuario> datos = new ArrayList<>();
        public void guardar(Usuario u) { datos.add(u); }
        public List<Usuario> listar() { return new ArrayList<>(datos); }
        public Usuario buscarPorEmail(String email) { return datos.stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst().orElse(null); }
        public Usuario buscarPorId(String id) { return datos.stream().filter(u -> u.getId().equals(id)).findFirst().orElse(null); }
        public void actualizar(Usuario u) { datos.removeIf(x -> x.getId().equals(u.getId())); datos.add(u); }
        public void eliminar(String email) { datos.removeIf(u -> u.getEmail().equalsIgnoreCase(email)); }
    }
}
