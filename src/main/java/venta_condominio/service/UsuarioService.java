package venta_condominio.service;

import org.springframework.stereotype.Service;
import venta_condominio.exception.UsuarioException;
import venta_condominio.model.Rol;
import venta_condominio.model.Usuario;
import venta_condominio.repository.UsuarioRepositorio;

@Service
public class UsuarioService {

    private final UsuarioRepositorio usuarioRepositorio;
    private final VerificacionEmailService verificacionEmailService;

    public UsuarioService(
            UsuarioRepositorio usuarioRepositorio,
            VerificacionEmailService verificacionEmailService) {

        this.usuarioRepositorio = usuarioRepositorio;
        this.verificacionEmailService = verificacionEmailService;
    }

    public void registrarUsuario(
            String id,
            String nombre,
            String email,
            String contrasena,
            Rol rol) {

        Usuario usuarioExistente =
                usuarioRepositorio.buscarPorEmail(email);

        if (usuarioExistente != null) {
            throw new UsuarioException(
                    "Ya existe un usuario registrado con ese correo"
            );
        }

        if (rol == Rol.CLIENTE &&
                !verificacionEmailService.estaVerificado(email)) {

            throw new UsuarioException(
                    "El correo electrónico debe ser verificado antes de crear la cuenta"
            );
        }

        Usuario usuario = new Usuario(
                id,
                nombre,
                email,
                contrasena,
                rol,
                true
        );

        usuarioRepositorio.guardar(usuario);
    }
}