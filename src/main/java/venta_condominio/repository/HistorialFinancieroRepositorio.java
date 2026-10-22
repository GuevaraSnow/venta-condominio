package venta_condominio.repository;

import java.util.List;

import venta_condominio.model.CambioDatosFinancieros;

public interface HistorialFinancieroRepositorio {

    void guardar(CambioDatosFinancieros cambio);

    List<CambioDatosFinancieros> listar();
}
