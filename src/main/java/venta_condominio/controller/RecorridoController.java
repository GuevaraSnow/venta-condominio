package venta_condominio.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import venta_condominio.model.AccionCotizacionRequest;
import venta_condominio.model.Recorrido;
import venta_condominio.model.RecorridoRequest;
import venta_condominio.service.RecorridoService;

/** F-02 Recorridos por el conjunto. Errores de negocio → HTTP 400 (GlobalExceptionHandler). */
@RestController
@RequestMapping("/recorridos")
public class RecorridoController {

    private final RecorridoService recorridoService;

    public RecorridoController(RecorridoService recorridoService) {
        this.recorridoService = recorridoService;
    }

    @PostMapping
    public ResponseEntity<Recorrido> agendar(@RequestBody RecorridoRequest request) {
        return ResponseEntity.ok(recorridoService.agendar(request));
    }

    /** Todos, o solo los de un cliente con ?cliente=correo */
    @GetMapping
    public ResponseEntity<List<Recorrido>> listar(@RequestParam(required = false) String cliente) {

        if (cliente != null && !cliente.isBlank()) {
            return ResponseEntity.ok(recorridoService.listarPorCliente(cliente));
        }

        return ResponseEntity.ok(recorridoService.listar());
    }

    @PutMapping("/{id}/iniciar")
    public ResponseEntity<Recorrido> iniciar(@PathVariable String id, @RequestBody AccionCotizacionRequest request) {
        return ResponseEntity.ok(recorridoService.iniciar(id, request.getEmail()));
    }

    @PutMapping("/{id}/finalizar")
    public ResponseEntity<Recorrido> finalizar(@PathVariable String id, @RequestBody AccionCotizacionRequest request) {
        return ResponseEntity.ok(recorridoService.finalizar(id, request.getEmail()));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Recorrido> cancelar(@PathVariable String id, @RequestBody AccionCotizacionRequest request) {
        return ResponseEntity.ok(recorridoService.cancelar(id, request.getEmail()));
    }
}
