package venta_condominio.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import venta_condominio.model.Unidad;
import venta_condominio.service.UnidadService;

@RestController
@RequestMapping("/unidades")
@CrossOrigin(origins = "http://localhost:5173")
public class UnidadController {

    private final UnidadService unidadService;

    public UnidadController(UnidadService unidadService) {
        this.unidadService = unidadService;
    }

    @GetMapping
    public ResponseEntity<List<Unidad>> listarTodas() {
        return ResponseEntity.ok(unidadService.listarTodas());
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<Unidad>> listarDisponibles() {
        return ResponseEntity.ok(unidadService.listarDisponibles());
    }

    @GetMapping("/{identificador}")
    public ResponseEntity<?> obtenerPorIdentificador(@PathVariable String identificador) {

        Unidad unidad = unidadService.obtenerPorIdentificador(identificador);

        if (unidad == null) {
            return ResponseEntity.status(404).body(
                    Map.of("mensaje", "No se encontró la unidad " + identificador)
            );
        }

        return ResponseEntity.ok(unidad);
    }

    @PostMapping
    public ResponseEntity<?> registrarUnidad(@RequestBody Unidad unidad) {

        try {
            unidadService.registrarUnidad(unidad);

            return ResponseEntity.ok(
                    Map.of("mensaje", "Unidad registrada correctamente")
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