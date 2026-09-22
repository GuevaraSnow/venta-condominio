package venta_condominio.repository;

import java.util.List;

import venta_condominio.model.Usuario;

public interface UsuarioRepositorio {

    void guardar(Usuario usuario);

    List<Usuario> listar();

    Usuario buscarPorEmail(String email);

    Usuario buscarPorId(String id);

    void actualizar(Usuario usuario);

    void eliminar(String email);
}