package venta_condominio.service;

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

    public void registrarCliente(RegistroClienteRequest request) {

        String email = request.getEmail();

        if (!verificacionEmailService.estaVerificado(email)) {
            throw new UsuarioException(
                    "El correo electrónico debe ser verificado antes de registrar el cliente"
            );
        }

        if (clienteRepositorio.buscarPorEmail(email) != null) {
            throw new UsuarioException(
                    "Ya existe un cliente registrado con ese correo"
            );
        }

        Municipio municipio = new Municipio(
                request.getMunicipio(),
                request.getDepartamento()
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
                municipio
        );

        String idUsuario = UUID.randomUUID().toString();

        usuarioService.registrarUsuario(
                idUsuario,
                request.getNombre(),
                request.getEmail(),
                request.getContrasena(),
                Rol.CLIENTE
        );

        clienteRepositorio.guardar(cliente);
    }
}