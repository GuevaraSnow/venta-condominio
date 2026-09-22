package venta_condominio.repository;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import venta_condominio.model.Cliente;

@Repository
public class ClienteRepositorioJson implements ClienteRepositorio {

    private final String rutaArchivo = "clientes.json";
    private final ObjectMapper objectMapper;

    public ClienteRepositorioJson() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void guardar(Cliente cliente) {

        try {
            File archivo = new File(rutaArchivo);

            List<Cliente> clientes = listar();

            clientes.add(cliente);

            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(archivo, clientes);

        } catch (Exception e) {
            throw new RuntimeException("No se pudo guardar el cliente", e);
        }
    }

    @Override
    public List<Cliente> listar() {

        try {
            File archivo = new File(rutaArchivo);

            if (!archivo.exists()) {
                return new ArrayList<>();
            }

            Cliente[] clientes = objectMapper.readValue(
                    archivo,
                    Cliente[].class
            );

            return new ArrayList<>(Arrays.asList(clientes));

        } catch (Exception e) {
            throw new RuntimeException("No se pudieron cargar los clientes", e);
        }
    }

    @Override
    public Cliente buscarPorEmail(String email) {

        List<Cliente> clientes = listar();

        for (Cliente cliente : clientes) {

            if (cliente.getEmail().equalsIgnoreCase(email)) {
                return cliente;
            }
        }

        return null;
    }

    @Override
    public void eliminar(String email) {

        List<Cliente> clientes = listar();

        boolean eliminado = clientes.removeIf(
                cliente -> cliente.getEmail().equalsIgnoreCase(email)
        );

        if (!eliminado) {
            return;
        }

        try {
            File archivo = new File(rutaArchivo);

            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(archivo, clientes);

        } catch (Exception e) {
            throw new RuntimeException("No se pudo eliminar el cliente", e);
        }
    }
}