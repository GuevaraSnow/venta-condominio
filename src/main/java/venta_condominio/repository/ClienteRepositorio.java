package venta_condominio.repository;

import java.util.List;

import venta_condominio.model.Cliente;

public interface ClienteRepositorio {

    void guardar(Cliente cliente);

    List<Cliente> listar();

    Cliente buscarPorEmail(String email);
}