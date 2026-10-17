package venta_condominio.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

import venta_condominio.exception.ValidacionRecorridoException;
import venta_condominio.model.Cliente;
import venta_condominio.model.EstadoRecorrido;
import venta_condominio.model.EstadoUnidad;
import venta_condominio.model.Recorrido;
import venta_condominio.model.RecorridoRequest;
import venta_condominio.model.Rol;
import venta_condominio.model.Unidad;
import venta_condominio.model.Usuario;
import venta_condominio.repository.ClienteRepositorio;
import venta_condominio.repository.RecorridoRepositorio;
import venta_condominio.repository.UnidadRepositorio;
import venta_condominio.repository.UsuarioRepositorio;

/**
 * F-02 Recorrer el conjunto: agendar, iniciar, finalizar y cancelar visitas.
 *
 *  - R001: solo clientes con registro completo pueden visitar una unidad
 *  - Horario de visitas: 8:00 a 17:00, desde hoy en adelante
 *  - Al iniciar, la unidad pasa a EN_RECORRIDO; al finalizar vuelve a DISPONIBLE (F-12)
 */
@Service
public class RecorridoService {

    public static final LocalTime HORA_INICIO = LocalTime.of(8, 0);
    public static final LocalTime HORA_FIN = LocalTime.of(17, 0);

    private final RecorridoRepositorio recorridoRepositorio;
    private final UnidadRepositorio unidadRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    private Supplier<LocalDate> hoy = LocalDate::now;

    public RecorridoService(RecorridoRepositorio recorridoRepositorio,
                            UnidadRepositorio unidadRepositorio,
                            ClienteRepositorio clienteRepositorio,
                            UsuarioRepositorio usuarioRepositorio) {
        this.recorridoRepositorio = recorridoRepositorio;
        this.unidadRepositorio = unidadRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    void setHoy(Supplier<LocalDate> hoy) {
        this.hoy = hoy;
    }

    // =====================================================
    // Agendar (cliente para sí mismo, o asesor/director para un cliente)
    // =====================================================

    public Recorrido agendar(RecorridoRequest request) {

        if (request == null) {
            throw new ValidacionRecorridoException("Faltan los datos del recorrido");
        }

        Usuario quien = buscarUsuario(request.getEmail());

        if (quien == null) {
            throw new ValidacionRecorridoException("Inicia sesión para agendar un recorrido");
        }

        String emailCliente = quien.getRol() == Rol.CLIENTE ? quien.getEmail() : texto(request.getEmailCliente());

        // R001: sin registro, sin visita
        Cliente cliente = emailCliente == null ? null : clienteRepositorio.buscarPorEmail(emailCliente);

        if (cliente == null) {
            throw new ValidacionRecorridoException("El cliente debe estar registrado para agendar un recorrido (R001)");
        }

        Unidad unidad = unidadRepositorio.buscarPorIdentificador(texto(request.getIdentificadorUnidad()));

        if (unidad == null) {
            throw new ValidacionRecorridoException("No se encontró la unidad " + request.getIdentificadorUnidad());
        }

        if (unidad.getEstado() != EstadoUnidad.DISPONIBLE) {
            throw new ValidacionRecorridoException("La unidad " + unidad.getIdentificador()
                    + " no está disponible (" + unidad.getEstado() + ")");
        }

        LocalDate fecha;
        LocalTime hora;

        try {
            fecha = LocalDate.parse(texto(request.getFecha()));
            hora = LocalTime.parse(texto(request.getHora()));
        } catch (DateTimeParseException | NullPointerException e) {
            throw new ValidacionRecorridoException("Indica una fecha y una hora válidas");
        }

        if (fecha.isBefore(hoy.get())) {
            throw new ValidacionRecorridoException("La fecha del recorrido no puede ser anterior a hoy");
        }

        if (hora.isBefore(HORA_INICIO) || hora.isAfter(HORA_FIN)) {
            throw new ValidacionRecorridoException("Los recorridos se agendan entre las 8:00 y las 17:00");
        }

        boolean ocupado = recorridoRepositorio.listar().stream().anyMatch(r ->
                r.getIdentificadorUnidad().equalsIgnoreCase(unidad.getIdentificador())
                        && r.getFecha().equals(fecha.toString())
                        && r.getHora().equals(hora.toString())
                        && (r.getEstado() == EstadoRecorrido.AGENDADO || r.getEstado() == EstadoRecorrido.EN_CURSO));

        if (ocupado) {
            throw new ValidacionRecorridoException("Ya hay un recorrido agendado para esa unidad a esa hora");
        }

        Recorrido recorrido = new Recorrido(
                UUID.randomUUID().toString(),
                unidad.getIdentificador(),
                cliente.getEmail(),
                (cliente.getNombre() + " " + cliente.getPrimerApellido()).trim(),
                quien.getEmail(),
                fecha.toString(),
                hora.toString(),
                EstadoRecorrido.AGENDADO,
                null
        );

        recorridoRepositorio.guardar(recorrido);

        return recorrido;
    }

    // =====================================================
    // Consultar
    // =====================================================

    public List<Recorrido> listar() {
        return recorridoRepositorio.listar().stream()
                .sorted(Comparator.comparing(Recorrido::getFecha).thenComparing(Recorrido::getHora))
                .toList();
    }

    public List<Recorrido> listarPorCliente(String emailCliente) {
        String email = emailCliente == null ? "" : emailCliente.trim();
        return listar().stream().filter(r -> r.getEmailCliente().equalsIgnoreCase(email)).toList();
    }

    // =====================================================
    // Iniciar / finalizar (asesor o director)
    // =====================================================

    public Recorrido iniciar(String id, String emailEquipo) {

        Usuario asesor = exigirEquipo(emailEquipo);
        Recorrido recorrido = buscar(id);

        if (recorrido.getEstado() != EstadoRecorrido.AGENDADO) {
            throw new ValidacionRecorridoException("Solo se inicia un recorrido agendado");
        }

        Unidad unidad = unidadRepositorio.buscarPorIdentificador(recorrido.getIdentificadorUnidad());

        if (unidad == null || unidad.getEstado() != EstadoUnidad.DISPONIBLE) {
            throw new ValidacionRecorridoException("La unidad ya no está disponible para el recorrido");
        }

        unidadRepositorio.actualizar(unidad.conEstado(EstadoUnidad.EN_RECORRIDO));

        Recorrido enCurso = recorrido.conEstado(EstadoRecorrido.EN_CURSO, asesor.getEmail());
        recorridoRepositorio.actualizar(enCurso);

        return enCurso;
    }

    public Recorrido finalizar(String id, String emailEquipo) {

        Usuario asesor = exigirEquipo(emailEquipo);
        Recorrido recorrido = buscar(id);

        if (recorrido.getEstado() != EstadoRecorrido.EN_CURSO) {
            throw new ValidacionRecorridoException("Solo se finaliza un recorrido en curso");
        }

        Unidad unidad = unidadRepositorio.buscarPorIdentificador(recorrido.getIdentificadorUnidad());

        if (unidad != null && unidad.getEstado() == EstadoUnidad.EN_RECORRIDO) {
            unidadRepositorio.actualizar(unidad.conEstado(EstadoUnidad.DISPONIBLE));
        }

        Recorrido finalizado = recorrido.conEstado(EstadoRecorrido.FINALIZADO, asesor.getEmail());
        recorridoRepositorio.actualizar(finalizado);

        return finalizado;
    }

    // =====================================================
    // Cancelar (el cliente dueño o el equipo comercial)
    // =====================================================

    public Recorrido cancelar(String id, String email) {

        Usuario quien = buscarUsuario(email);
        Recorrido recorrido = buscar(id);

        boolean esElCliente = quien != null && quien.getEmail().equalsIgnoreCase(recorrido.getEmailCliente());
        boolean esEquipo = quien != null && quien.getRol() != Rol.CLIENTE;

        if (!esElCliente && !esEquipo) {
            throw new ValidacionRecorridoException("No puedes cancelar este recorrido");
        }

        if (recorrido.getEstado() != EstadoRecorrido.AGENDADO) {
            throw new ValidacionRecorridoException("Solo se cancela un recorrido agendado");
        }

        Recorrido cancelado = recorrido.conEstado(EstadoRecorrido.CANCELADO, quien.getEmail());
        recorridoRepositorio.actualizar(cancelado);

        return cancelado;
    }

    // =====================================================
    // Utilidades
    // =====================================================

    private Recorrido buscar(String id) {
        Recorrido recorrido = recorridoRepositorio.buscarPorId(id);

        if (recorrido == null) {
            throw new ValidacionRecorridoException("No se encontró el recorrido");
        }

        return recorrido;
    }

    private Usuario exigirEquipo(String email) {
        Usuario usuario = buscarUsuario(email);

        if (usuario == null || usuario.getRol() == Rol.CLIENTE) {
            throw new ValidacionRecorridoException("Solo el asesor o el director comercial atienden recorridos");
        }

        return usuario;
    }

    private Usuario buscarUsuario(String email) {
        return email == null || email.isBlank() ? null : usuarioRepositorio.buscarPorEmail(email.trim());
    }

    private static String texto(String valor) {
        return valor == null ? null : valor.trim();
    }
}
