package venta_condominio.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

import venta_condominio.exception.ValidacionCotizacionException;
import venta_condominio.model.AccionCotizacionRequest;
import venta_condominio.model.Cliente;
import venta_condominio.model.Cotizacion;
import venta_condominio.model.CotizacionRequest;
import venta_condominio.model.EstadoCotizacion;
import venta_condominio.model.EstadoUnidad;
import venta_condominio.model.Rol;
import venta_condominio.model.Unidad;
import venta_condominio.model.Usuario;
import venta_condominio.repository.ClienteRepositorio;
import venta_condominio.repository.CotizacionRepositorio;
import venta_condominio.repository.UnidadRepositorio;
import venta_condominio.repository.UsuarioRepositorio;
import venta_condominio.patrones.CotizacionFactory;
import venta_condominio.patrones.EventoVenta;
import venta_condominio.patrones.PublicadorEventos;

/**
 * F-03 Generar cotización.
 *
 * Condiciones acordadas con el negocio:
 *  - descuento máximo 10 %; si supera el 2 % la autoriza el director comercial (R003)
 *  - la fabricación de la cotización (cálculos, vigencia, estado inicial) la hace
 *    CotizacionFactory (patrón Factory)
 *  - cada cambio importante se notifica a los observadores (patrón Observer)
 */
@Service
public class CotizacionService extends PublicadorEventos {

    public static final BigDecimal DESCUENTO_MAXIMO = new BigDecimal("10");

    private final CotizacionFactory fabrica = new CotizacionFactory();

    private final CotizacionRepositorio cotizacionRepositorio;
    private final UnidadRepositorio unidadRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    /** Permite fijar "hoy" en las pruebas. */
    private Supplier<LocalDate> hoy = LocalDate::now;

    public CotizacionService(CotizacionRepositorio cotizacionRepositorio,
                             UnidadRepositorio unidadRepositorio,
                             ClienteRepositorio clienteRepositorio,
                             UsuarioRepositorio usuarioRepositorio) {
        this.cotizacionRepositorio = cotizacionRepositorio;
        this.unidadRepositorio = unidadRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    void setHoy(Supplier<LocalDate> hoy) {
        this.hoy = hoy;
    }

    // =====================================================
    // Crear
    // =====================================================

    public Cotizacion crear(CotizacionRequest request) {

        if (request == null) {
            throw new ValidacionCotizacionException("Faltan los datos de la cotización");
        }

        Usuario asesor = exigirRol(request.getEmailAsesor(),
                "Solo el asesor o el director comercial pueden generar cotizaciones",
                Rol.ASESOR_COMERCIAL, Rol.DIRECTOR_COMERCIAL);

        Cliente cliente = clienteRepositorio.buscarPorEmail(texto(request.getEmailCliente()));

        if (cliente == null) {
            throw new ValidacionCotizacionException("El cliente no está registrado");
        }

        Unidad unidad = unidadRepositorio.buscarPorIdentificador(texto(request.getIdentificadorUnidad()));

        if (unidad == null) {
            throw new ValidacionCotizacionException("No se encontró la unidad " + request.getIdentificadorUnidad());
        }

        // F-12: solo se cotizan unidades disponibles
        if (unidad.getEstado() != EstadoUnidad.DISPONIBLE) {
            throw new ValidacionCotizacionException("La unidad " + unidad.getIdentificador()
                    + " no está disponible (" + unidad.getEstado() + ")");
        }

        BigDecimal descuento = request.getPorcentajeDescuento() == null
                ? BigDecimal.ZERO : request.getPorcentajeDescuento();

        if (descuento.compareTo(BigDecimal.ZERO) < 0 || descuento.compareTo(DESCUENTO_MAXIMO) > 0) {
            throw new ValidacionCotizacionException("El descuento debe estar entre 0 % y "
                    + DESCUENTO_MAXIMO.toPlainString() + " %");
        }

        // Patrón Factory: la fábrica aplica cálculos, vigencia y estado inicial (R003)
        Cotizacion cotizacion = fabrica.crear(unidad, cliente, asesor, descuento, hoy.get());

        cotizacionRepositorio.guardar(cotizacion);

        // Patrón Observer
        if (cotizacion.getEstado() == EstadoCotizacion.GENERADA) {
            notificar(new EventoVenta("COTIZACION_GENERADA", cotizacion.getEmailCliente(),
                    "Tienes una nueva cotización",
                    "Hola " + cotizacion.getNombreCliente() + ", tu asesor generó la cotización de "
                            + cotizacion.getIdentificadorUnidad() + " por " + pesos(cotizacion.getPrecioFinal())
                            + ". Ingresa a Venta Condominio para firmarla antes del "
                            + cotizacion.getFechaVencimiento() + "."));
        } else {
            notificar(new EventoVenta("DESCUENTO_POR_AUTORIZAR", null,
                    "Descuento por autorizar",
                    "La cotización de " + cotizacion.getIdentificadorUnidad() + " tiene un descuento de "
                            + cotizacion.getPorcentajeDescuento() + " % que requiere autorización (R003)."));
        }

        return cotizacion;
    }

    // Cálculos (KAN-96) y regla R003 (KAN-97): ver CotizacionFactory

    // =====================================================
    // Consultar (marca las vencidas, KAN-99)
    // =====================================================

    public List<Cotizacion> listar() {

        List<Cotizacion> resultado = new ArrayList<>();

        for (Cotizacion cotizacion : cotizacionRepositorio.listar()) {
            resultado.add(revisarVencimiento(cotizacion));
        }

        resultado.sort(Comparator.comparing(Cotizacion::getFechaCreacion,
                Comparator.nullsLast(Comparator.reverseOrder())));

        return resultado;
    }

    public List<Cotizacion> listarPorCliente(String emailCliente) {
        return listar().stream()
                .filter(c -> c.getEmailCliente().equalsIgnoreCase(texto(emailCliente)))
                .toList();
    }

    public List<Cotizacion> listarPendientes() {
        return listar().stream()
                .filter(c -> c.getEstado() == EstadoCotizacion.PENDIENTE_AUTORIZACION)
                .toList();
    }

    private Cotizacion revisarVencimiento(Cotizacion cotizacion) {

        if (cotizacion.estaVencida(hoy.get())) {
            Cotizacion vencida = cotizacion.conEstado(EstadoCotizacion.VENCIDA, null,
                    "Venció sin firmarse");
            cotizacionRepositorio.actualizar(vencida);
            return vencida;
        }

        return cotizacion;
    }

    private Cotizacion buscar(String id) {

        Cotizacion cotizacion = cotizacionRepositorio.buscarPorId(id);

        if (cotizacion == null) {
            throw new ValidacionCotizacionException("No se encontró la cotización");
        }

        return revisarVencimiento(cotizacion);
    }

    // =====================================================
    // Autorizar / rechazar (director comercial)
    // =====================================================

    public Cotizacion autorizar(String id, AccionCotizacionRequest request) {
        return decidir(id, request, EstadoCotizacion.GENERADA);
    }

    public Cotizacion rechazar(String id, AccionCotizacionRequest request) {
        return decidir(id, request, EstadoCotizacion.RECHAZADA);
    }

    private Cotizacion decidir(String id, AccionCotizacionRequest request, EstadoCotizacion nuevoEstado) {

        Usuario director = exigirRol(request == null ? null : request.getEmail(),
                "Solo el director comercial puede autorizar descuentos", Rol.DIRECTOR_COMERCIAL);

        Cotizacion cotizacion = buscar(id);

        if (cotizacion.getEstado() != EstadoCotizacion.PENDIENTE_AUTORIZACION) {
            throw new ValidacionCotizacionException("La cotización no está pendiente de autorización (estado "
                    + cotizacion.getEstado() + ")");
        }

        Cotizacion actualizada = cotizacion.conEstado(nuevoEstado, director.getEmail(),
                request.getObservacion());

        cotizacionRepositorio.actualizar(actualizada);

        boolean autorizada = nuevoEstado == EstadoCotizacion.GENERADA;
        notificar(new EventoVenta(autorizada ? "DESCUENTO_AUTORIZADO" : "DESCUENTO_RECHAZADO",
                actualizada.getEmailCliente(),
                autorizada ? "Tu descuento fue autorizado" : "Tu descuento no fue aprobado",
                autorizada
                        ? "El director comercial autorizó el descuento de tu cotización de "
                          + actualizada.getIdentificadorUnidad() + ". Ya puedes firmarla en Venta Condominio."
                        : "El director comercial no aprobó el descuento de tu cotización de "
                          + actualizada.getIdentificadorUnidad() + ". Tu asesor te contactará."));

        return actualizada;
    }

    // =====================================================
    // Firmar (cliente)
    // =====================================================

    public Cotizacion firmar(String id, AccionCotizacionRequest request) {

        String email = request == null ? null : texto(request.getEmail());
        Cotizacion cotizacion = buscar(id);

        if (email == null || !cotizacion.getEmailCliente().equalsIgnoreCase(email)) {
            throw new ValidacionCotizacionException("Solo el cliente de la cotización puede firmarla");
        }

        switch (cotizacion.getEstado()) {
            case GENERADA -> { }
            case PENDIENTE_AUTORIZACION -> throw new ValidacionCotizacionException(
                    "El descuento aún no ha sido autorizado por el director comercial");
            case VENCIDA -> throw new ValidacionCotizacionException(
                    "La cotización venció el " + cotizacion.getFechaVencimiento());
            default -> throw new ValidacionCotizacionException(
                    "La cotización no se puede firmar (estado " + cotizacion.getEstado() + ")");
        }

        Unidad unidad = unidadRepositorio.buscarPorIdentificador(cotizacion.getIdentificadorUnidad());

        if (unidad == null || unidad.getEstado() != EstadoUnidad.DISPONIBLE) {
            throw new ValidacionCotizacionException("La unidad ya no está disponible");
        }

        Cotizacion firmada = cotizacion.conEstado(EstadoCotizacion.FIRMADA, null, null);
        cotizacionRepositorio.actualizar(firmada);

        notificar(new EventoVenta("COTIZACION_FIRMADA", firmada.getEmailAsesor(),
                "Cotización firmada",
                firmada.getNombreCliente() + " firmó la cotización de " + firmada.getIdentificadorUnidad()
                        + ". Ya puede reservar la unidad."));

        return firmada;
    }

    // =====================================================
    // Utilidades
    // =====================================================

    private Usuario exigirRol(String email, String mensaje, Rol... roles) {

        Usuario usuario = email == null ? null : usuarioRepositorio.buscarPorEmail(texto(email));

        if (usuario != null) {
            for (Rol rol : roles) {
                if (usuario.getRol() == rol) {
                    return usuario;
                }
            }
        }

        throw new ValidacionCotizacionException(mensaje);
    }

    private static String nombreCompleto(Cliente cliente) {
        return (cliente.getNombre() + " " + cliente.getPrimerApellido() + " "
                + (cliente.getSegundoApellido() == null ? "" : cliente.getSegundoApellido())).trim();
    }

    static String pesos(BigDecimal valor) {
        return "$" + java.text.NumberFormat.getIntegerInstance(java.util.Locale.of("es", "CO")).format(valor);
    }

    private static String texto(String valor) {
        return valor == null ? null : valor.trim();
    }
}
