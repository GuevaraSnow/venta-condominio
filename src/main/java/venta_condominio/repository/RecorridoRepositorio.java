package venta_condominio.repository;

import java.util.List;

import venta_condominio.model.Recorrido;

public interface RecorridoRepositorio {

    void guardar(Recorrido recorrido);

    List<Recorrido> listar();

    Recorrido buscarPorId(String id);

    void actualizar(Recorrido recorrido);
}
