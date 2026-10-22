package venta_condominio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import venta_condominio.model.DatosFinancierosRequest;
import venta_condominio.model.RegistroClienteRequest;
import venta_condominio.service.ClienteService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/clientes")
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

    /** Valida el registro antes de enviar el código al correo. No guarda nada. */
    @PostMapping("/validar")
    public ResponseEntity<Map<String, String>> validarRegistro(
            @RequestBody RegistroClienteRequest request) {

        try {
            clienteService.validarRegistro(request);
            return ResponseEntity.ok(Map.of("mensaje", "Datos válidos"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    /** Resumen de clientes para el formulario de cotización (F-03). */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listarClientes() {
        return ResponseEntity.ok(clienteService.listarResumen());
    }

    /** Salario y presupuesto actuales del cliente (director comercial o el mismo cliente). */
    @GetMapping("/{emailCliente}/datos-financieros")
    public ResponseEntity<?> datosFinancieros(@PathVariable String emailCliente,
                                              @RequestParam(required = false) String email) {
        try {
            return ResponseEntity.ok(clienteService.datosFinancieros(emailCliente, email));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    /** Solo el director comercial: actualiza salario/presupuesto y deja registro en el historial. */
    @PutMapping("/{emailCliente}/datos-financieros")
    public ResponseEntity<?> actualizarDatosFinancieros(@PathVariable String emailCliente,
                                                        @RequestBody DatosFinancierosRequest request) {
        try {
            clienteService.actualizarDatosFinancieros(emailCliente, request);
            return ResponseEntity.ok(Map.of("mensaje", "Datos financieros actualizados"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    /** Historial de cambios de datos financieros (auditoría). */
    @GetMapping("/{emailCliente}/historial-financiero")
    public ResponseEntity<?> historialFinanciero(@PathVariable String emailCliente,
                                                 @RequestParam(required = false) String email) {
        try {
            return ResponseEntity.ok(clienteService.historialFinanciero(emailCliente, email));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    /** Qué unidades disponibles están al alcance del cliente (precalificación). */
    @GetMapping("/{emailCliente}/alcance")
    public ResponseEntity<?> alcance(@PathVariable String emailCliente) {
        try {
            return ResponseEntity.ok(clienteService.alcance(emailCliente));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }
}
