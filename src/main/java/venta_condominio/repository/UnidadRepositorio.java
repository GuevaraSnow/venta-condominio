package venta_condominio.repository;

import java.util.List;

import venta_condominio.model.EstadoUnidad;
import venta_condominio.model.Unidad;

public interface UnidadRepositorio {

    void guardar(Unidad unidad);

    List<Unidad> listar();

    Unidad buscarPorIdentificador(String identificador);

    List<Unidad> listarPorEstado(EstadoUnidad estado);

    void actualizar(Unidad unidad);
}