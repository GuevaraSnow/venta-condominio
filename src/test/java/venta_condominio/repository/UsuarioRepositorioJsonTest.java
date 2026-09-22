package venta_condominio.repository;

import org.junit.jupiter.api.Test;
import venta_condominio.model.Rol;
import venta_condominio.model.Usuario;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioRepositorioJsonTest {

    @Test
    void debeGuardarYBuscarUsuario() {

        UsuarioRepositorioJson repositorio = new UsuarioRepositorioJson();

        Usuario usuario = new Usuario(
                "USR-001",
                "Santiago",
                "santiago@gmail.com",
                "123456",
                Rol.CLIENTE,
                true
        );

        repositorio.guardar(usuario);

        Usuario encontrado = repositorio.buscarPorEmail("santiago@gmail.com");

        assertNotNull(encontrado);
        assertEquals("Santiago", encontrado.getNombre());
        assertEquals(Rol.CLIENTE, encontrado.getRol());
    }
}