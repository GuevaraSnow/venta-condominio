package venta_condominio.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Repository;
import venta_condominio.model.Usuario;

import java.io.File;
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

        File archivo = new File(rutaArchivo);

        if (!archivo.exists()) {
            return new ArrayList<>();
        }

        try {
            Usuario[] usuarios = objectMapper.readValue(
                    archivo,
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
    public void eliminar(String email) {

        List<Usuario> usuarios = listar();

        usuarios.removeIf(usuario ->
                usuario.getEmail().equalsIgnoreCase(email)
        );

        guardarLista(usuarios);
    }

    private void guardarLista(List<Usuario> usuarios) {

        try {
            objectMapper.writeValue(
                    new File(rutaArchivo),
                    usuarios
            );

        } catch (IOException e) {
            throw new RuntimeException("Error al escribir usuarios.json", e);
        }
    }
}