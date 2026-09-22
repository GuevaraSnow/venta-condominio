package venta_condominio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import venta_condominio.model.RegistroClienteRequest;
import venta_condominio.service.ClienteService;

import java.util.Map;

@RestController
@RequestMapping("/clientes")
@CrossOrigin(origins = "http://localhost:5173")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<?> registrarCliente(
            @RequestBody RegistroClienteRequest request) {

        try {
            clienteService.registrarCliente(request);

            return ResponseEntity.ok(
                    Map.of("mensaje", "Cliente registrado correctamente")
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "mensaje", e.getMessage(),
                            "tipo", e.getClass().getSimpleName()
                    )
            );
        }
    }
}