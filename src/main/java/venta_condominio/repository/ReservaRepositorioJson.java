package venta_condominio.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import venta_condominio.model.Reserva;
import venta_condominio.util.Almacenamiento;

/** Guarda las reservas en reservas.json (MongoDB en producción). */
@Repository
public class ReservaRepositorioJson implements ReservaRepositorio {

    private final String rutaArchivo = "reservas.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void guardar(Reserva reserva) {
        List<Reserva> reservas = listar();
        reservas.add(reserva);
        escribir(reservas, "No se pudo guardar la reserva");
    }

    @Override
    public List<Reserva> listar() {

        String contenido = Almacenamiento.leerTexto(rutaArchivo);
        List<Reserva> reservas = new ArrayList<>();

        if (contenido == null || contenido.isBlank()) {
            return reservas;
        }

        try {
            JsonNode raiz = objectMapper.readTree(contenido);

            if (raiz == null || !raiz.isArray()) {
                return reservas;
            }

            int indice = 0;

            // Se lee registro por registro: uno dañado no tumba toda la lista
            for (JsonNode nodo : raiz) {
                try {
                    reservas.add(objectMapper.treeToValue(nodo, Reserva.class));
                } catch (Exception errorRegistro) {
                    System.err.println("Se omitió el registro #" + indice + " de " + rutaArchivo
                            + ": " + errorRegistro.getMessage());
                }
                indice++;
            }

            return reservas;

        } catch (IOException e) {
            throw new RuntimeException("No se pudieron cargar las reservas", e);
        }
    }

    @Override
    public Reserva buscarPorId(String id) {

        for (Reserva reserva : listar()) {
            if (reserva.getId().equals(id)) {
                return reserva;
            }
        }

        return null;
    }

    @Override
    public void actualizar(Reserva reserva) {
        List<Reserva> reservas = listar();

        for (int i = 0; i < reservas.size(); i++) {
            if (reservas.get(i).getId().equals(reserva.getId())) {
                reservas.set(i, reserva);
                escribir(reservas, "No se pudo actualizar la reserva");
                return;
            }
        }

        reservas.add(reserva);
        escribir(reservas, "No se pudo actualizar la reserva");
    }

    private void escribir(List<Reserva> reservas, String mensajeError) {
        try {
            Almacenamiento.escribirTexto(rutaArchivo,
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(reservas));
        } catch (Exception e) {
            throw new RuntimeException(mensajeError, e);
        }
    }
}
