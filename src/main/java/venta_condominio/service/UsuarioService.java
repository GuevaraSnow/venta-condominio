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
                true,
                null
        );

        usuarioRepositorio.guardar(usuario);
    }

    public Usuario iniciarSesion(String email, String contrasena) {

        if (email == null || email.isBlank() || contrasena == null || contrasena.isBlank()) {
            throw new UsuarioException("Correo y contraseña son obligatorios");
        }

        Usuario usuario = usuarioRepositorio.buscarPorEmail(email);

        if (usuario == null) {
            throw new UsuarioException("Correo o contraseña incorrectos");
        }

        if (!usuario.isActivo()) {
            throw new UsuarioException("La cuenta está inactiva");
        }

        if (!usuario.getContrasena().equals(contrasena)) {
            throw new UsuarioException("Correo o contraseña incorrectos");
        }

        return usuario;
    }

    public Usuario actualizarFotoPerfil(String id, String fotoBase64) {

        Usuario usuario = usuarioRepositorio.buscarPorId(id);

        if (usuario == null) {
            throw new UsuarioException("Usuario no encontrado");
        }

        if (fotoBase64 == null || fotoBase64.isBlank()) {
            throw new UsuarioException("La foto es obligatoria");
        }

        Usuario actualizado = usuario.conFotoPerfil(fotoBase64);

        usuarioRepositorio.actualizar(actualizado);

        return actualizado;
    }
}