package venta_condominio.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import venta_condominio.exception.UsuarioException;
import venta_condominio.model.Cliente;
import venta_condominio.model.Municipio;
import venta_condominio.model.RegistroClienteRequest;
import venta_condominio.model.Rol;
import venta_condominio.repository.ClienteRepositorio;

@Service
public class ClienteService {

    private final ClienteRepositorio clienteRepositorio;
    private final UsuarioService usuarioService;
    private final VerificacionEmailService verificacionEmailService;

    public ClienteService(
            ClienteRepositorio clienteRepositorio,
            UsuarioService usuarioService,
            VerificacionEmailService verificacionEmailService) {

        this.clienteRepositorio = clienteRepositorio;
        this.usuarioService = usuarioService;
        this.verificacionEmailService = verificacionEmailService;
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
}
