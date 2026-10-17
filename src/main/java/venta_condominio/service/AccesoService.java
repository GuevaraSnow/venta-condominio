package venta_condominio.service;

import org.springframework.stereotype.Service;

import venta_condominio.exception.AccesoDenegadoException;
import venta_condominio.model.Rol;
import venta_condominio.model.Usuario;
import venta_condominio.repository.ClienteRepositorio;
import venta_condominio.repository.UsuarioRepositorio;

/**
 * R001 "Sin registro, sin visita": solo un usuario activo puede ver las unidades,
 * y si es cliente debe tener su registro completo (ficha de cliente guardada).
 */
@Service
public class AccesoService {

    private final UsuarioRepositorio usuarioRepositorio;
    private final ClienteRepositorio clienteRepositorio;

    public AccesoService(UsuarioRepositorio usuarioRepositorio, ClienteRepositorio clienteRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.clienteRepositorio = clienteRepositorio;
    }

    public Usuario exigirPuedeVerUnidades(String email) {

        Usuario usuario = (email == null || email.isBlank())
                ? null
                : usuarioRepositorio.buscarPorEmail(email.trim());

        if (usuario == null || !usuario.isActivo()) {
            throw new AccesoDenegadoException("Inicia sesión para ver las unidades disponibles");
        }

        if (usuario.getRol() == Rol.CLIENTE && clienteRepositorio.buscarPorEmail(usuario.getEmail()) == null) {
            throw new AccesoDenegadoException("Completa tu registro de cliente para ver las unidades (R001)");
        }

        return usuario;
    }
}
