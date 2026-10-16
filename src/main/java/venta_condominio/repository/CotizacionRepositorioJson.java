package venta_condominio.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import venta_condominio.model.Cotizacion;
import venta_condominio.util.Almacenamiento;

/** Guarda las cotizaciones en cotizaciones.json (MongoDB en producción). */
@Repository
public class CotizacionRepositorioJson implements CotizacionRepositorio {

    private final String rutaArchivo = "cotizaciones.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void guardar(Cotizacion cotizacion) {
        List<Cotizacion> cotizaciones = listar();
        cotizaciones.add(cotizacion);
        escribir(cotizaciones, "No se pudo guardar la cotización");
    }

    @Override
    public List<Cotizacion> listar() {

        String contenido = Almacenamiento.leerTexto(rutaArchivo);
        List<Cotizacion> cotizaciones = new ArrayList<>();

        if (contenido == null || contenido.isBlank()) {
            return cotizaciones;
        }

        try {
            JsonNode raiz = objectMapper.readTree(contenido);

            if (raiz == null || !raiz.isArray()) {
                return cotizaciones;
            }

            int indice = 0;

            // Se lee registro por registro: uno dañado no tumba toda la lista
            for (JsonNode nodo : raiz) {
                try {
                    cotizaciones.add(objectMapper.treeToValue(nodo, Cotizacion.class));
                } catch (Exception errorRegistro) {
                    System.err.println("Se omitió el registro #" + indice + " de " + rutaArchivo
                            + ": " + errorRegistro.getMessage());
                }
                indice++;
            }

            return cotizaciones;

        } catch (IOException e) {
            throw new RuntimeException("No se pudieron cargar las cotizaciones", e);
        }
    }

    @Override
    public Cotizacion buscarPorId(String id) {

        for (Cotizacion cotizacion : listar()) {
            if (cotizacion.getId().equals(id)) {
                return cotizacion;
            }
        }

        return null;
    }

    @Override
    public void actualizar(Cotizacion cotizacion) {
        List<Cotizacion> cotizaciones = listar();

        for (int i = 0; i < cotizaciones.size(); i++) {
            if (cotizaciones.get(i).getId().equals(cotizacion.getId())) {
                cotizaciones.set(i, cotizacion);
                escribir(cotizaciones, "No se pudo actualizar la cotización");
                return;
            }
        }

        cotizaciones.add(cotizacion);
        escribir(cotizaciones, "No se pudo actualizar la cotización");
    }

    private void escribir(List<Cotizacion> cotizaciones, String mensajeError) {
        try {
            Almacenamiento.escribirTexto(rutaArchivo,
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(cotizaciones));
        } catch (Exception e) {
            throw new RuntimeException(mensajeError, e);
        }
    }
}
