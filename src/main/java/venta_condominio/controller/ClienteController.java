package venta_condominio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import venta_condominio.model.RegistroClienteRequest;
import venta_condominio.service.ClienteService;

@RestController
@RequestMapping("/clientes")
@CrossOrigin(origins = "http://localhost:5173")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<String> registrarCliente(
            @RequestBody RegistroClienteRequest request) {

        clienteService.registrarCliente(request);

        return ResponseEntity.ok(
                "Cliente registrado correctamente"
        );
    }
}