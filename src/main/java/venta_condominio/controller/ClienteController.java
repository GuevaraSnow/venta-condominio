package venta_condominio.controller;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import venta_condominio.model.Cliente;
import venta_condominio.model.Departamento;
import venta_condominio.model.Municipio;
import venta_condominio.service.ClienteService;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<String> registrarCliente(@RequestBody Cliente cliente) {

        clienteService.registrarCliente(
                cliente.getNombre(),
                cliente.getPrimerApellido(),
                cliente.getSegundoApellido(),
                cliente.getEmail(),
                cliente.getTelefono(),
                cliente.getSalario(),
                cliente.getPresupuesto(),
                cliente.getDepartamento(),
                cliente.getMunicipio()
        );

        return ResponseEntity.ok("Cliente registrado correctamente");
    }
}