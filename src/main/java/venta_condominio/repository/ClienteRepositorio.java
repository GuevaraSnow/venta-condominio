package venta_condominio.repository;

import java.util.List;

import venta_condominio.model.Cliente;

public interface ClienteRepositorio {

    void guardar(Cliente cliente);

    List<Cliente> listar();

    Cliente buscarPorEmail(String email);

    default Cliente buscarPorTelefono(String telefono) {
        if (telefono == null) {
            return null;
        }

        return listar().stream()
                .filter(c -> telefono.trim().equals(c.getTelefono()))
                .findFirst()
                .orElse(null);
    }

    default Cliente buscarPorCedula(String cedula) {
        if (cedula == null) {
            return null;
        }

        return listar().stream()
                .filter(c -> cedula.trim().equals(c.getCedula()))
                .findFirst()
                .orElse(null);
    }

    void eliminar(String email);
}