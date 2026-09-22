package venta_condominio.service;

import org.springframework.stereotype.Service;
import venta_condominio.model.Rol;
import venta_condominio.model.Usuario;
import venta_condominio.repository.UsuarioRepositorio;
import venta_condominio.exception.UsuarioException;

@Service
public class UsuarioService {

    private final UsuarioRepositorio usuarioRepositorio;

    public UsuarioService(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
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