package venta_condominio.repository;

import org.springframework.stereotype.Repository;

import venta_condominio.model.Recorrido;

/**
 * Guarda los registros en recorridos.json (MongoDB en producción).
 * Subclase concreta del patrón Template Method: solo define los pasos variables.
 */
@Repository
public class RecorridoRepositorioJson extends RepositorioJsonBase<Recorrido> implements RecorridoRepositorio {

    @Override
    protected String rutaArchivo() {
        return "recorridos.json";
    }

    @Override
    protected Class<Recorrido> tipo() {
        return Recorrido.class;
    }

    @Override
    protected String idDe(Recorrido registro) {
        return registro.getId();
    }
}
