package venta_condominio.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import venta_condominio.model.Recorrido;
import venta_condominio.util.Almacenamiento;

/** Guarda los recorridos en recorridos.json (MongoDB en producción). */
@Repository
public class RecorridoRepositorioJson implements RecorridoRepositorio {

    private final String rutaArchivo = "recorridos.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void guardar(Recorrido recorrido) {
        List<Recorrido> recorridos = listar();
        recorridos.add(recorrido);
        escribir(recorridos, "No se pudo guardar el recorrido");
    }

    @Override
    public List<Recorrido> listar() {

        String contenido = Almacenamiento.leerTexto(rutaArchivo);
        List<Recorrido> recorridos = new ArrayList<>();

        if (contenido == null || contenido.isBlank()) {
            return recorridos;
        }

        try {
            JsonNode raiz = objectMapper.readTree(contenido);

            if (raiz == null || !raiz.isArray()) {
                return recorridos;
            }

            int indice = 0;

            // Se lee registro por registro: uno dañado no tumba toda la lista
            for (JsonNode nodo : raiz) {
                try {
                    recorridos.add(objectMapper.treeToValue(nodo, Recorrido.class));
                } catch (Exception errorRegistro) {
                    System.err.println("Se omitió el registro #" + indice + " de " + rutaArchivo
                            + ": " + errorRegistro.getMessage());
                }
                indice++;
            }

            return recorridos;

        } catch (IOException e) {
            throw new RuntimeException("No se pudieron cargar los recorridos", e);
        }
    }

    @Override
    public Recorrido buscarPorId(String id) {

        for (Recorrido recorrido : listar()) {
            if (recorrido.getId().equals(id)) {
                return recorrido;
            }
        }

        return null;
    }

    @Override
    public void actualizar(Recorrido recorrido) {
        List<Recorrido> recorridos = listar();

        for (int i = 0; i < recorridos.size(); i++) {
            if (recorridos.get(i).getId().equals(recorrido.getId())) {
                recorridos.set(i, recorrido);
                escribir(recorridos, "No se pudo actualizar el recorrido");
                return;
            }
        }

        recorridos.add(recorrido);
        escribir(recorridos, "No se pudo actualizar el recorrido");
    }

    private void escribir(List<Recorrido> recorridos, String mensajeError) {
        try {
            Almacenamiento.escribirTexto(rutaArchivo,
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(recorridos));
        } catch (Exception e) {
            throw new RuntimeException(mensajeError, e);
        }
    }
}
