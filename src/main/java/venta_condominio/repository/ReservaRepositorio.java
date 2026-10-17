package venta_condominio.repository;

import java.util.List;

import venta_condominio.model.Reserva;

public interface ReservaRepositorio {

    void guardar(Reserva reserva);

    List<Reserva> listar();

    Reserva buscarPorId(String id);

    void actualizar(Reserva reserva);
}
