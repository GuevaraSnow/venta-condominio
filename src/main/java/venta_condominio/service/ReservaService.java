package venta_condominio.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

import venta_condominio.exception.ValidacionReservaException;
import venta_condominio.model.Cotizacion;
import venta_condominio.model.EstadoCotizacion;
import venta_condominio.model.EstadoReserva;
import venta_condominio.model.EstadoUnidad;
import venta_condominio.model.Reserva;
import venta_condominio.model.ReservaRequest;
import venta_condominio.model.Rol;
import venta_condominio.model.Unidad;
import venta_condominio.model.Usuario;
import venta_condominio.repository.CotizacionRepositorio;
import venta_condominio.repository.ReservaRepositorio;
import venta_condominio.repository.UnidadRepositorio;
import venta_condominio.repository.UsuarioRepositorio;
import venta_condominio.patrones.EventoVenta;
import venta_condominio.patrones.PublicadorEventos;
import venta_condominio.patrones.ReservaFactory;

/**
 * F-04 Reservar unidad y calcular cuota de reserva.
 *
 *  - R002: cuota de reserva = 1 % del precio de lista, mínimo $5.000.000
 *  - R004: solo se reserva con la cotización FIRMADA por el cliente
 *  - F-12: la unidad debe estar DISPONIBLE; al reservar pasa a RESERVADA
 */
@Service
public class ReservaService extends PublicadorEventos {

    private final ReservaFactory fabrica = new ReservaFactory();

    private final ReservaRepositorio reservaRepositorio;
    private final CotizacionRepositorio cotizacionRepositorio;
    private final UnidadRepositorio unidadRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    private Supplier<LocalDate> hoy = LocalDate::now;

    public ReservaService(ReservaRepositorio reservaRepositorio,
                          CotizacionRepositorio cotizacionRepositorio,
                          UnidadRepositorio unidadRepositorio,
                          UsuarioRepositorio usuarioRepositorio) {
        this.reservaRepositorio = reservaRepositorio;
        this.cotizacionRepositorio = cotizacionRepositorio;
        this.unidadRepositorio = unidadRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    void setHoy(Supplier<LocalDate> hoy) {
        this.hoy = hoy;
    }

    // R002: cálculo de la cuota de reserva → ver ReservaFactory.calcularCuota

    public static BigDecimal calcularCuota(BigDecimal precioLista) {
        return ReservaFactory.calcularCuota(precioLista);
    }

    // =====================================================
    // Reservar
    // =====================================================

    public Reserva reservar(ReservaRequest request) {

        if (request == null || request.getIdCotizacion() == null) {
            throw new ValidacionReservaException("Indica la cotización a reservar");
        }

        Cotizacion cotizacion = cotizacionRepositorio.buscarPorId(request.getIdCotizacion());

        if (cotizacion == null) {
            throw new ValidacionReservaException("No se encontró la cotización");
        }

        Usuario quien = buscarUsuario(request.getEmail());
        boolean esElCliente = quien != null && quien.getEmail().equalsIgnoreCase(cotizacion.getEmailCliente());
        boolean esEquipo = quien != null
                && (quien.getRol() == Rol.ASESOR_COMERCIAL || quien.getRol() == Rol.DIRECTOR_COMERCIAL);

        if (!esElCliente && !esEquipo) {
            throw new ValidacionReservaException("Solo el cliente de la cotización o el equipo comercial pueden reservar");
        }

        // R004: la cotización debe estar firmada
        if (cotizacion.getEstado() != EstadoCotizacion.FIRMADA) {
            throw new ValidacionReservaException("Debe firmar la cotización antes de reservar");
        }

        boolean yaReservada = reservaRepositorio.listar().stream()
                .anyMatch(r -> r.getIdCotizacion().equals(cotizacion.getId())
                        && r.getEstado() != EstadoReserva.CANCELADA);

        if (yaReservada) {
            throw new ValidacionReservaException("Esta cotización ya tiene una reserva activa");
        }

        Unidad unidad = unidadRepositorio.buscarPorIdentificador(cotizacion.getIdentificadorUnidad());

        if (unidad == null) {
            throw new ValidacionReservaException("No se encontró la unidad " + cotizacion.getIdentificadorUnidad());
        }

        // F-12: no se reserva una unidad que no esté disponible
        if (unidad.getEstado() != EstadoUnidad.DISPONIBLE) {
            throw new ValidacionReservaException("La unidad " + unidad.getIdentificador()
                    + " no está disponible (" + unidad.getEstado() + ")");
        }

        // Patrón Factory: la fábrica arma la reserva y calcula la cuota (R002)
        Reserva reserva = fabrica.crear(cotizacion, unidad, quien.getEmail(), hoy.get());

        reservaRepositorio.guardar(reserva);
        unidadRepositorio.actualizar(unidad.conEstado(EstadoUnidad.RESERVADA));

        // Patrón Observer
        notificar(new EventoVenta("UNIDAD_RESERVADA", reserva.getEmailCliente(),
                "Reserva confirmada: " + reserva.getIdentificadorUnidad(),
                "Hola " + reserva.getNombreCliente() + ", reservaste " + reserva.getIdentificadorUnidad()
                        + ". Cuota de reserva: " + CotizacionService.pesos(reserva.getCuotaReserva())
                        + ". La unidad queda bloqueada a tu nombre mientras se valida el pago."));

        return reserva;
    }

    // =====================================================
    // Consultar
    // =====================================================

    public List<Reserva> listar() {
        return reservaRepositorio.listar().stream()
                .sorted(Comparator.comparing(Reserva::getFechaReserva,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public List<Reserva> listarPorCliente(String emailCliente) {
        String email = emailCliente == null ? "" : emailCliente.trim();
        return listar().stream()
                .filter(r -> r.getEmailCliente().equalsIgnoreCase(email))
                .toList();
    }

    // =====================================================
    // Cancelar (equipo comercial): la unidad vuelve a DISPONIBLE
    // =====================================================

    public Reserva cancelar(String id, String emailEquipo) {

        Usuario quien = buscarUsuario(emailEquipo);

        if (quien == null || quien.getRol() == Rol.CLIENTE) {
            throw new ValidacionReservaException("Solo el equipo comercial puede cancelar reservas");
        }

        Reserva reserva = reservaRepositorio.buscarPorId(id);

        if (reserva == null) {
            throw new ValidacionReservaException("No se encontró la reserva");
        }

        if (reserva.getEstado() != EstadoReserva.PENDIENTE_PAGO) {
            throw new ValidacionReservaException("Solo se cancelan reservas pendientes de pago");
        }

        Reserva cancelada = reserva.conEstado(EstadoReserva.CANCELADA, "Cancelada por " + quien.getEmail());
        reservaRepositorio.actualizar(cancelada);

        Unidad unidad = unidadRepositorio.buscarPorIdentificador(reserva.getIdentificadorUnidad());

        if (unidad != null && unidad.getEstado() == EstadoUnidad.RESERVADA) {
            unidadRepositorio.actualizar(unidad.conEstado(EstadoUnidad.DISPONIBLE));
        }

        notificar(new EventoVenta("RESERVA_CANCELADA", cancelada.getEmailCliente(),
                "Reserva cancelada: " + cancelada.getIdentificadorUnidad(),
                "Tu reserva de " + cancelada.getIdentificadorUnidad() + " fue cancelada por el equipo comercial."));

        return cancelada;
    }

    private Usuario buscarUsuario(String email) {
        return email == null ? null : usuarioRepositorio.buscarPorEmail(email.trim());
    }
}
