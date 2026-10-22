package venta_condominio.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import venta_condominio.exception.UsuarioException;
import venta_condominio.model.CambioDatosFinancieros;
import venta_condominio.model.Cliente;
import venta_condominio.model.DatosFinancierosRequest;
import venta_condominio.model.EstadoUnidad;
import venta_condominio.model.Unidad;
import venta_condominio.model.Usuario;
import venta_condominio.model.Municipio;
import venta_condominio.model.RegistroClienteRequest;
import venta_condominio.model.Rol;
import venta_condominio.repository.ClienteRepositorio;
import venta_condominio.repository.HistorialFinancieroRepositorio;
import venta_condominio.repository.UnidadRepositorio;
import venta_condominio.repository.UsuarioRepositorio;

@Service
public class ClienteService {

    /**
     * El presupuesto es el dinero disponible para la CUOTA INICIAL.
     * Mínimo: la cuota de reserva más baja (R002, $5.000.000).
     */
    private static final BigDecimal PRESUPUESTO_MINIMO = new BigDecimal("5000000");

    private final ClienteRepositorio clienteRepositorio;
    private final UsuarioService usuarioService;
    private final VerificacionEmailService verificacionEmailService;
    private final UsuarioRepositorio usuarioRepositorio;
    private final UnidadRepositorio unidadRepositorio;
    private final HistorialFinancieroRepositorio historialRepositorio;

    public ClienteService(
            ClienteRepositorio clienteRepositorio,
            UsuarioService usuarioService,
            VerificacionEmailService verificacionEmailService,
            UsuarioRepositorio usuarioRepositorio,
            UnidadRepositorio unidadRepositorio,
            HistorialFinancieroRepositorio historialRepositorio) {

        this.clienteRepositorio = clienteRepositorio;
        this.usuarioService = usuarioService;
        this.verificacionEmailService = verificacionEmailService;
        this.usuarioRepositorio = usuarioRepositorio;
        this.unidadRepositorio = unidadRepositorio;
        this.historialRepositorio = historialRepositorio;
    }

    /**
     * Valida todos los datos del registro SIN guardar nada.
     * Se llama antes de enviar el código de verificación al correo,
     * para que el usuario vea los errores de una vez.
     */
    public Cliente validarRegistro(RegistroClienteRequest request) {

        if (request == null) {
            throw new UsuarioException("Faltan los datos del registro");
        }

        if (!request.isAceptoTratamientoDatos()) {
            throw new UsuarioException(
                    "Debes aceptar el tratamiento de datos personales (Ley 1581 de 2012) para registrarte"
            );
        }

        if (request.getContrasena() == null || request.getContrasena().length() < 6) {
            throw new UsuarioException("La contraseña debe tener al menos 6 caracteres");
        }

        String cedula = limpio(request.getCedula());
        String telefono = limpio(request.getTelefono());
        String email = limpio(request.getEmail());

        if (cedula == null || !cedula.matches("\\d{6,10}")) {
            throw new UsuarioException("La cédula debe tener entre 6 y 10 dígitos, solo números");
        }

        if (telefono == null || !telefono.matches("3\\d{9}")) {
            throw new UsuarioException(
                    "El teléfono debe ser un celular de 10 dígitos que empiece por 3, solo números");
        }

        if (email == null || !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new UsuarioException("El correo electrónico no es válido");
        }

        if (request.getSalario() == null || request.getSalario().compareTo(BigDecimal.ZERO) <= 0) {
            throw new UsuarioException("Los ingresos mensuales del hogar deben ser mayores a cero");
        }

        if (request.getPresupuesto() == null || request.getPresupuesto().compareTo(PRESUPUESTO_MINIMO) < 0) {
            throw new UsuarioException("El dinero disponible para la cuota inicial debe ser de al menos $ 5.000.000");
        }

        Municipio municipio = new Municipio(
                request.getMunicipio(),
                request.getDepartamento()
        );

        Cliente cliente = construirCliente(request, municipio);

        if (clienteRepositorio.buscarPorEmail(cliente.getEmail()) != null
                || usuarioService.existeUsuario(cliente.getEmail())) {
            throw new UsuarioException(
                    "Ya existe una cuenta registrada con ese correo"
            );
        }

        if (clienteRepositorio.buscarPorCedula(cliente.getCedula()) != null) {
            throw new UsuarioException(
                    "Ya existe un cliente registrado con esa cédula"
            );
        }

        if (clienteRepositorio.buscarPorTelefono(cliente.getTelefono()) != null) {
            throw new UsuarioException(
                    "Ya existe un cliente registrado con ese teléfono"
            );
        }

        return cliente;
    }

    public void registrarCliente(RegistroClienteRequest request) {

        Cliente cliente = validarRegistro(request);

        if (!verificacionEmailService.estaVerificado(cliente.getEmail())) {
            throw new UsuarioException(
                    "El correo electrónico debe ser verificado antes de registrar el cliente"
            );
        }

        String idUsuario = UUID.randomUUID().toString();

        usuarioService.registrarUsuario(
                idUsuario,
                request.getNombre(),
                request.getEmail(),
                request.getContrasena(),
                Rol.CLIENTE,
                true
        );

        clienteRepositorio.guardar(cliente);
    }

    private static String limpio(String valor) {
        return valor == null ? null : valor.trim();
    }

    private Cliente construirCliente(RegistroClienteRequest request, Municipio municipio) {

        return new Cliente(
                limpio(request.getNombre()),
                limpio(request.getPrimerApellido()),
                limpio(request.getSegundoApellido()),
                limpio(request.getCedula()),
                limpio(request.getEmail()),
                limpio(request.getTelefono()),
                request.getSalario(),
                request.getPresupuesto(),
                request.getTipoInmueble(),
                request.getDepartamento(),
                municipio
        );
    }

    /**
     * Lista resumida de clientes para que el asesor elija a quién cotizar (F-03).
     * No expone cédula, teléfono ni salario.
     */
    public List<Map<String, Object>> listarResumen() {

        List<Map<String, Object>> resumen = new ArrayList<>();

        for (Cliente cliente : clienteRepositorio.listar()) {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("nombre", (cliente.getNombre() + " " + cliente.getPrimerApellido()).trim());
            fila.put("email", cliente.getEmail());
            fila.put("tipoInmueble", cliente.getTipoInmueble());
            fila.put("presupuesto", cliente.getPresupuesto());
            fila.put("municipio", cliente.getMunicipio() != null ? cliente.getMunicipio().getNombre() : null);
            resumen.add(fila);
        }

        return resumen;
    }

    // =====================================================
    // Datos financieros (solo el director comercial los cambia)
    // =====================================================

    /**
     * El cliente NO puede cambiar su salario ni su presupuesto: los declara una vez
     * al registrarse. Solo el DIRECTOR COMERCIAL los actualiza (el asesor solo hace
     * recorridos y cotizaciones), después de revisar
     * soportes, y cada cambio queda en el historial (auditoría).
     */
    public Cliente actualizarDatosFinancieros(String emailCliente, DatosFinancierosRequest request) {

        if (request == null) {
            throw new UsuarioException("Faltan los datos a actualizar");
        }

        Usuario director = exigirDirector(request.getEmailAsesor());
        Cliente cliente = exigirCliente(emailCliente);

        if (request.getSalario() == null || request.getSalario().compareTo(BigDecimal.ZERO) <= 0) {
            throw new UsuarioException("Los ingresos mensuales del hogar deben ser mayores a cero");
        }

        if (request.getPresupuesto() == null || request.getPresupuesto().compareTo(PRESUPUESTO_MINIMO) < 0) {
            throw new UsuarioException("El dinero disponible para la cuota inicial debe ser de al menos $ 5.000.000");
        }

        String motivo = limpio(request.getMotivo());

        if (motivo == null || motivo.length() < 5) {
            throw new UsuarioException("Indica el soporte que justifica el cambio (ej. certificado laboral, extracto bancario)");
        }

        if (request.getSalario().compareTo(cliente.getSalario()) == 0
                && request.getPresupuesto().compareTo(cliente.getPresupuesto()) == 0) {
            throw new UsuarioException("Los valores son iguales a los actuales; no hay nada que cambiar");
        }

        Cliente actualizado = cliente.conDatosFinancieros(request.getSalario(), request.getPresupuesto());
        clienteRepositorio.actualizar(actualizado);

        historialRepositorio.guardar(new CambioDatosFinancieros(
                UUID.randomUUID().toString(),
                cliente.getEmail(),
                cliente.getSalario(),
                actualizado.getSalario(),
                cliente.getPresupuesto(),
                actualizado.getPresupuesto(),
                director.getEmail(),
                LocalDateTime.now().withNano(0).toString(),
                motivo
        ));

        return actualizado;
    }

    /** Salario y presupuesto actuales: los ve el director comercial o el propio cliente. */
    public Map<String, Object> datosFinancieros(String emailCliente, String emailSolicitante) {

        Cliente cliente = exigirCliente(emailCliente);
        exigirClienteODirector(cliente, emailSolicitante);

        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("email", cliente.getEmail());
        datos.put("nombre", (cliente.getNombre() + " " + cliente.getPrimerApellido()).trim());
        datos.put("salario", cliente.getSalario());
        datos.put("presupuesto", cliente.getPresupuesto());
        return datos;
    }

    /** Historial de cambios de salario/presupuesto de un cliente (más reciente primero). */
    public List<CambioDatosFinancieros> historialFinanciero(String emailCliente, String emailSolicitante) {

        exigirDirector(emailSolicitante);

        return historialRepositorio.listar().stream()
                .filter(c -> c.getEmailCliente().equalsIgnoreCase(limpio(emailCliente)))
                .sorted((a, b) -> String.valueOf(b.getFecha()).compareTo(String.valueOf(a.getFecha())))
                .toList();
    }

    /**
     * Para cada unidad disponible, si está al alcance del cliente
     * (precalificación sin descuento). Lo usa el listado de unidades.
     */
    public List<Map<String, Object>> alcance(String emailCliente) {

        Cliente cliente = exigirCliente(emailCliente);
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Unidad unidad : unidadRepositorio.listarPorEstado(EstadoUnidad.DISPONIBLE)) {

            Precalificacion.Resultado r = Precalificacion.evaluar(cliente, unidad.getPrecioLista(), BigDecimal.ZERO);

            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("identificador", unidad.getIdentificador());
            fila.put("alcanza", r.aprobado());
            fila.put("cuotaInicial", r.cuotaInicial());
            fila.put("cuotaMensual", r.cuotaMensual());
            fila.put("salarioMinimo", r.salarioMinimo());
            fila.put("motivo", r.motivo());
            resultado.add(fila);
        }

        return resultado;
    }

    private Cliente exigirCliente(String emailCliente) {

        Cliente cliente = emailCliente == null ? null : clienteRepositorio.buscarPorEmail(limpio(emailCliente));

        if (cliente == null) {
            throw new UsuarioException("No se encontró el cliente");
        }

        return cliente;
    }

    private Usuario exigirDirector(String email) {

        Usuario usuario = email == null ? null : usuarioRepositorio.buscarPorEmail(limpio(email));

        if (usuario == null || !usuario.isActivo() || usuario.getRol() != Rol.DIRECTOR_COMERCIAL) {
            throw new UsuarioException("Solo el director comercial puede consultar o cambiar los datos financieros de un cliente");
        }

        return usuario;
    }

    private void exigirClienteODirector(Cliente cliente, String emailSolicitante) {

        if (emailSolicitante != null && cliente.getEmail().equalsIgnoreCase(limpio(emailSolicitante))) {
            return;
        }

        exigirDirector(emailSolicitante);
    }
}
