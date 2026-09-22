package venta_condominio.service;

import java.util.List;

import org.springframework.stereotype.Service;

import venta_condominio.model.EstadoUnidad;
import venta_condominio.model.Unidad;
import venta_condominio.repository.UnidadRepositorio;

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
}