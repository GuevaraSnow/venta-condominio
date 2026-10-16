package venta_condominio.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import venta_condominio.model.AccionCotizacionRequest;
import venta_condominio.model.Cotizacion;
import venta_condominio.model.CotizacionRequest;
import venta_condominio.service.CotizacionService;

/**
 * F-03 Generar cotización. Los errores de negocio (ValidacionCotizacionException)
 * los convierte GlobalExceptionHandler en HTTP 400 con {"mensaje": ...}.
 */
@RestController
@RequestMapping("/cotizaciones")
public class CotizacionController {

    private final CotizacionService cotizacionService;

    public CotizacionController(CotizacionService cotizacionService) {
        this.cotizacionService = cotizacionService;
    }

    @PostMapping
    public ResponseEntity<Cotizacion> crear(@RequestBody CotizacionRequest request) {
        return ResponseEntity.ok(cotizacionService.crear(request));
    }

    /** Todas, o solo las de un cliente con ?cliente=correo */
    @GetMapping
    public ResponseEntity<List<Cotizacion>> listar(@RequestParam(required = false) String cliente) {

        if (cliente != null && !cliente.isBlank()) {
            return ResponseEntity.ok(cotizacionService.listarPorCliente(cliente));
        }

        return ResponseEntity.ok(cotizacionService.listar());
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<Cotizacion>> pendientes() {
        return ResponseEntity.ok(cotizacionService.listarPendientes());
    }

    @PutMapping("/{id}/autorizar")
    public ResponseEntity<Cotizacion> autorizar(@PathVariable String id,
                                                @RequestBody AccionCotizacionRequest request) {
        return ResponseEntity.ok(cotizacionService.autorizar(id, request));
    }

    @PutMapping("/{id}/rechazar")
    public ResponseEntity<Cotizacion> rechazar(@PathVariable String id,
                                               @RequestBody AccionCotizacionRequest request) {
        return ResponseEntity.ok(cotizacionService.rechazar(id, request));
    }

    @PutMapping("/{id}/firmar")
    public ResponseEntity<Cotizacion> firmar(@PathVariable String id,
                                             @RequestBody AccionCotizacionRequest request) {
        return ResponseEntity.ok(cotizacionService.firmar(id, request));
    }
}
