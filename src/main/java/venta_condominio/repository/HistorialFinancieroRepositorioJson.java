package venta_condominio.repository;

import org.springframework.stereotype.Repository;

import venta_condominio.model.CambioDatosFinancieros;

/** Guarda la auditoría en historial_financiero.json (MongoDB en producción). */
@Repository
public class HistorialFinancieroRepositorioJson extends RepositorioJsonBase<CambioDatosFinancieros>
        implements HistorialFinancieroRepositorio {

    @Override
    protected String rutaArchivo() {
        return "historial_financiero.json";
    }

    @Override
    protected Class<CambioDatosFinancieros> tipo() {
        return CambioDatosFinancieros.class;
    }

    @Override
    protected String idDe(CambioDatosFinancieros registro) {
        return registro.getId();
    }
}
