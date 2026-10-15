package venta_condominio.repository;

import venta_condominio.util.Almacenamiento;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Repository;
import venta_condominio.model.Usuario;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
public class UsuarioRepositorioJson implements UsuarioRepositorio {

    private final ObjectMapper objectMapper;
    private final String rutaArchivo = "usuarios.json";

    public UsuarioRepositorioJson() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void guardar(Usuario usuario) {

        List<Usuario> usuarios = listar();

        usuarios.add(usuario);

        guardarLista(usuarios);
    }

    @Override
    public List<Usuario> listar() {

        String contenido = Almacenamiento.leerTexto(rutaArchivo);

        if (contenido == null || contenido.isBlank()) {
            return new ArrayList<>();
        }

        try {
            Usuario[] usuarios = objectMapper.readValue(
                    contenido,
                    Usuario[].class
            );

            return new ArrayList<>(Arrays.asList(usuarios));

        } catch (IOException e) {
            throw new RuntimeException("Error al leer usuarios.json", e);
        }
    }

    @Override
    public Usuario buscarPorEmail(String email) {

        List<Usuario> usuarios = listar();

        for (Usuario usuario : usuarios) {

            if (usuario.getEmail().equalsIgnoreCase(email)) {
                return usuario;
            }
        }

        return null;
    }

    @Override
    public Usuario buscarPorId(String id) {

        List<Usuario> usuarios = listar();

        for (Usuario usuario : usuarios) {

            if (usuario.getId().equals(id)) {
                return usuario;
            }
        }

        return null;
    }

    @Override
    public void actualizar(Usuario usuario) {

        List<Usuario> usuarios = listar();

        usuarios.removeIf(u -> u.getId().equals(usuario.getId()));
        usuarios.add(usuario);

        guardarLista(usuarios);
    }

    @Override
    public void eliminar(String email) {

        List<Usuario> usuarios = listar();

        usuarios.removeIf(usuario ->
                usuario.getEmail().equalsIgnoreCase(email)
        );

        guardarLista(usuarios);
    }

    private void guardarLista(List<Usuario> usuarios) {

        try {
            Almacenamiento.escribirTexto(
                    rutaArchivo,
                    objectMapper.writeValueAsString(usuarios)
            );

        } catch (IOException e) {
            throw new RuntimeException("Error al escribir usuarios.json", e);
        }
    }
}
