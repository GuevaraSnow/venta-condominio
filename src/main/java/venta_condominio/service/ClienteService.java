package venta_condominio.service;

import org.springframework.stereotype.Service;

import venta_condominio.model.Cliente;
import venta_condominio.repository.ClienteRepositorio;

import java.math.BigDecimal;

import venta_condominio.model.Departamento;
import venta_condominio.model.Municipio;

@Service
public class ClienteService {

    private final ClienteRepositorio clienteRepositorio;

    public ClienteService(ClienteRepositorio clienteRepositorio) {
        this.clienteRepositorio = clienteRepositorio;
    }

    public void registrarCliente(
            String nombre,
            String primerApellido,
            String segundoApellido,
            String email,
            String telefono,
            BigDecimal salario,
            BigDecimal presupuesto,
            Departamento departamento,
            Municipio municipio) {

        Cliente cliente = new Cliente(
                nombre,
                primerApellido,
                segundoApellido,
                email,
                telefono,
                salario,
                presupuesto,
                departamento,
                municipio
        );

        clienteRepositorio.guardar(cliente);
    }
}