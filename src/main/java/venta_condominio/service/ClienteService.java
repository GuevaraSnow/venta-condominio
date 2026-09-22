package venta_condominio.service;

import org.springframework.stereotype.Service;

import venta_condominio.exception.UsuarioException;
import venta_condominio.model.Cliente;
import venta_condominio.model.RegistroClienteRequest;
import venta_condominio.model.Rol;
import venta_condominio.repository.ClienteRepositorio;

import java.util.UUID;

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

    public void registrarCliente(RegistroClienteRequest request) {

        String email = request.getEmail();

        if (!verificacionEmailService.estaVerificado(email)) {
            throw new UsuarioException(
                    "El correo electrónico debe ser verificado antes de registrar el cliente"
            );
        }

        Cliente clienteExistente =
                clienteRepositorio.buscarPorEmail(email);

        if (clienteExistente != null) {
            throw new UsuarioException(
                    "Ya existe un cliente registrado con ese correo"
            );
        }

        String idUsuario = UUID.randomUUID().toString();

        usuarioService.registrarUsuario(
                idUsuario,
                request.getNombre(),
                request.getEmail(),
                request.getContrasena(),
                Rol.CLIENTE
        );

        Cliente cliente = new Cliente(
                request.getNombre(),
                request.getPrimerApellido(),
                request.getSegundoApellido(),
                request.getCedula(),
                request.getEmail(),
                request.getTelefono(),
                request.getSalario(),
                request.getPresupuesto(),
                request.getTipoInmueble(),
                request.getDepartamento(),
                request.getMunicipio()
        );

        clienteRepositorio.guardar(cliente);
    }
}