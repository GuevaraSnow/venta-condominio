package venta_condominio.service;

import java.util.List;

import org.springframework.stereotype.Service;

import venta_condominio.exception.ValidacionUnidadException;
import venta_condominio.model.EstadoUnidad;
import venta_condominio.model.Unidad;
import venta_condominio.repository.UnidadRepositorio;
import venta_condominio.util.GuardadorArchivos;

@Service
public class UnidadService {

    private final UnidadRepositorio unidadRepositorio;

    public UnidadService(UnidadRepositorio unidadRepositorio) {
        this.unidadRepositorio = unidadRepositorio;
    }

    public List<Unidad> listarTodas() {
        return unidadRepositorio.listar();
    }

    public List<Unidad> listarDisponibles() {
        return unidadRepositorio.listarPorEstado(EstadoUnidad.DISPONIBLE);
    }

    public Unidad obtenerPorIdentificador(String identificador) {
        return unidadRepositorio.buscarPorIdentificador(identificador);
    }

    public void registrarUnidad(Unidad unidad) {
        unidadRepositorio.guardar(unidad);
    }

    public Unidad agregarFoto(String identificador, String fotoBase64) {

        Unidad unidad = unidadRepositorio.buscarPorIdentificador(identificador);

        if (unidad == null) {
            throw new ValidacionUnidadException("No se encontró la unidad " + identificador);
        }

        if (fotoBase64 == null || fotoBase64.isBlank()) {
            throw new ValidacionUnidadException("La foto es obligatoria");
        }

        String rutaFoto = GuardadorArchivos.guardarImagenBase64(fotoBase64, "unidades");

        Unidad actualizada = unidad.conFotoAgregada(rutaFoto);

        unidadRepositorio.actualizar(actualizada);

        return actualizada;
    }
}