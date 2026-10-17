package venta_condominio.repository;

import org.springframework.stereotype.Repository;

import venta_condominio.model.Reserva;

/**
 * Guarda los registros en reservas.json (MongoDB en producción).
 * Subclase concreta del patrón Template Method: solo define los pasos variables.
 */
@Repository
public class ReservaRepositorioJson extends RepositorioJsonBase<Reserva> implements ReservaRepositorio {

    @Override
    protected String rutaArchivo() {
        return "reservas.json";
    }

    @Override
    protected Class<Reserva> tipo() {
        return Reserva.class;
    }

    @Override
    protected String idDe(Reserva registro) {
        return registro.getId();
    }
}
