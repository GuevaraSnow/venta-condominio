package venta_condominio.repository;

import java.util.List;

import venta_condominio.model.Cotizacion;

public interface CotizacionRepositorio {

    void guardar(Cotizacion cotizacion);

    List<Cotizacion> listar();

    Cotizacion buscarPorId(String id);

    void actualizar(Cotizacion cotizacion);
}
