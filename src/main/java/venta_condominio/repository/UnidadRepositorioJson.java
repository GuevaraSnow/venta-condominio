package venta_condominio.repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import venta_condominio.model.EstadoUnidad;
import venta_condominio.model.Unidad;

@Repository
public class UnidadRepositorioJson implements UnidadRepositorio {

    private final String rutaArchivo = "unidades.json";
    private final ObjectMapper objectMapper;

    public UnidadRepositorioJson() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void guardar(Unidad unidad) {

        try {
            List<Unidad> unidades = listar();
            unidades.add(unidad);

            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(rutaArchivo), unidades);

        } catch (Exception e) {
            throw new RuntimeException("No se pudo guardar la unidad", e);
        }
    }

    @Override
    public List<Unidad> listar() {

        File archivo = new File(rutaArchivo);

        if (!archivo.exists()) {
            return new ArrayList<>();
        }

        List<Unidad> unidades = new ArrayList<>();

        try {
            JsonNode raiz = objectMapper.readTree(archivo);

            if (raiz == null || !raiz.isArray()) {
                return unidades;
            }

            int indice = 0;

            for (JsonNode nodoUnidad : raiz) {

                try {
                    Unidad unidad = objectMapper.treeToValue(nodoUnidad, Unidad.class);
                    unidades.add(unidad);

                } catch (Exception errorRegistro) {
                    System.err.println(
                            "Se omitió el registro #" + indice + " de " + rutaArchivo
                                    + " por estar corrupto/incompleto: "
                                    + errorRegistro.getMessage()
                    );
                }

                indice++;
            }

            return unidades;

        } catch (IOException e) {
            throw new RuntimeException("No se pudieron cargar las unidades", e);
        }
    }

    @Override
    public Unidad buscarPorIdentificador(String identificador) {

        for (Unidad unidad : listar()) {
            if (unidad.getIdentificador().equalsIgnoreCase(identificador)) {
                return unidad;
            }
        }

        return null;
    }

    @Override
    public List<Unidad> listarPorEstado(EstadoUnidad estado) {

        List<Unidad> resultado = new ArrayList<>();

        for (Unidad unidad : listar()) {
            if (unidad.getEstado() == estado) {
                resultado.add(unidad);
            }
        }

        return resultado;
    }

    @Override
    public void actualizar(Unidad unidad) {

        List<Unidad> unidades = listar();

        unidades.removeIf(u -> u.getIdentificador().equalsIgnoreCase(unidad.getIdentificador()));
        unidades.add(unidad);

        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(rutaArchivo), unidades);

        } catch (Exception e) {
            throw new RuntimeException("No se pudo actualizar la unidad", e);
        }
    }
}