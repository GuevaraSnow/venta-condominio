package venta_condominio.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import venta_condominio.model.AccionCotizacionRequest;
import venta_condominio.model.Reserva;
import venta_condominio.model.ReservaRequest;
import venta_condominio.service.ReservaService;

/**
 * F-04 Reservar unidad. Los errores de negocio (ValidacionReservaException)
 * los convierte GlobalExceptionHandler en HTTP 400 con {"mensaje": ...}.
 */
@RestController
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping
    public ResponseEntity<Reserva> reservar(@RequestBody ReservaRequest request) {
        return ResponseEntity.ok(reservaService.reservar(request));
    }

    /** Todas, o solo las de un cliente con ?cliente=correo */
    @GetMapping
    public ResponseEntity<List<Reserva>> listar(@RequestParam(required = false) String cliente) {

        if (cliente != null && !cliente.isBlank()) {
            return ResponseEntity.ok(reservaService.listarPorCliente(cliente));
        }

        return ResponseEntity.ok(reservaService.listar());
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Reserva> cancelar(@PathVariable String id,
                                            @RequestBody AccionCotizacionRequest request) {
        return ResponseEntity.ok(reservaService.cancelar(id, request.getEmail()));
    }
}
