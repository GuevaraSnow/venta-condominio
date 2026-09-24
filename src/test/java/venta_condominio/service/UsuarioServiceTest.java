package venta_condominio.service;

import org.junit.jupiter.api.Test;
import venta_condominio.model.Rol;
import venta_condominio.model.Usuario;
import venta_condominio.repository.UsuarioRepositorio;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class UsuarioServiceTest {

    @Test
    void debeRegistrarUsuario() {

        UsuarioRepositorio repositorio =
                new RepositorioEnMemoria();

        ServicioEnvioEmail servicioEnvioEmail =
                mock(ServicioEnvioEmail.class);

        VerificacionEmailService verificacionEmailService =
                new VerificacionEmailService(
                        servicioEnvioEmail
                );

        UsuarioService service =
                new UsuarioService(
                        repositorio,
                        verificacionEmailService
                );

        verificacionEmailService.generarCodigo(
                "santiago@gmail.com"
        );

        verificacionEmailService.marcarComoVerificado(
                "santiago@gmail.com"
        );

        service.registrarUsuario(
                "USR-001",
                "Santiago",
                "santiago@gmail.com",
                "123456",
                Rol.CLIENTE
        );

        Usuario usuario =
                repositorio.buscarPorEmail(
                        "santiago@gmail.com"
                );

        assertNotNull(usuario);
        assertEquals("Santiago", usuario.getNombre());
        assertEquals(Rol.CLIENTE, usuario.getRol());
        assertTrue(usuario.isActivo());
    }

    @Test
    void noDebeRegistrarClienteSinVerificarCorreo() {

        UsuarioRepositorio repositorio =
                new RepositorioEnMemoria();

        ServicioEnvioEmail servicioEnvioEmail =
                mock(ServicioEnvioEmail.class);

        VerificacionEmailService verificacionEmailService =
                new VerificacionEmailService(
                        servicioEnvioEmail
                );

        UsuarioService service =
                new UsuarioService(
                        repositorio,
                        verificacionEmailService
                );

        assertThrows(
                RuntimeException.class,
                () -> service.registrarUsuario(
                        "USR-001",
                        "Santiago",
                        "santiago@gmail.com",
                        "123456",
                        Rol.CLIENTE
                )
        );
    }

    @Test
    void noDebeRegistrarDosUsuariosConElMismoEmail() {

        UsuarioRepositorio repositorio =
                new RepositorioEnMemoria();

        ServicioEnvioEmail servicioEnvioEmail =
                mock(ServicioEnvioEmail.class);

        VerificacionEmailService verificacionEmailService =
                new VerificacionEmailService(
                        servicioEnvioEmail
                );

        UsuarioService service =
                new UsuarioService(
                        repositorio,
                        verificacionEmailService
                );

        verificacionEmailService.generarCodigo(
                "santiago@gmail.com"
        );

        verificacionEmailService.marcarComoVerificado(
                "santiago@gmail.com"
        );

        service.registrarUsuario(
                "USR-001",
                "Santiago",
                "santiago@gmail.com",
                "123456",
                Rol.CLIENTE
        );

        assertThrows(
                RuntimeException.class,
                () -> service.registrarUsuario(
                        "USR-002",
                        "Carlos",
                        "santiago@gmail.com",
                        "654321",
                        Rol.ASESOR_COMERCIAL
                )
        );
    }

    private static class RepositorioEnMemoria
            implements UsuarioRepositorio {

        private final List<Usuario> usuarios =
                new ArrayList<>();

        @Override
        public void guardar(Usuario usuario) {
            usuarios.add(usuario);
        }

        @Override
        public List<Usuario> listar() {
            return usuarios;
        }

        @Override
        public Usuario buscarPorEmail(String email) {

            for (Usuario usuario : usuarios) {
                if (usuario.getEmail()
                        .equalsIgnoreCase(email)) {

                    return usuario;
                }
            }

            return null;
        }

        @Override
        public Usuario buscarPorId(String id) {

            for (Usuario usuario : usuarios) {
                if (usuario.getId().equals(id)) {
                    return usuario;
                }
            }

            return null;
        }

        @Override
        public void actualizar(Usuario usuario) {

            usuarios.removeIf(u ->
                    u.getId().equals(usuario.getId())
            );

            usuarios.add(usuario);
        }

        @Override
        public void eliminar(String email) {

            usuarios.removeIf(usuario ->
                    usuario.getEmail()
                            .equalsIgnoreCase(email)
            );
        }
    }
}