package venta_condominio.repository;

import org.springframework.stereotype.Repository;

import venta_condominio.model.Cotizacion;

/**
 * Guarda los registros en cotizaciones.json (MongoDB en producción).
 * Subclase concreta del patrón Template Method: solo define los pasos variables.
 */
@Repository
public class CotizacionRepositorioJson extends RepositorioJsonBase<Cotizacion> implements CotizacionRepositorio {

    @Override
    protected String rutaArchivo() {
        return "cotizaciones.json";
    }

    @Override
    protected Class<Cotizacion> tipo() {
        return Cotizacion.class;
    }

    @Override
    protected String idDe(Cotizacion registro) {
        return registro.getId();
    }
}
