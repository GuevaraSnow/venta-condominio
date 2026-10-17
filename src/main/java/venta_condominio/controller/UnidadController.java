package venta_condominio.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import venta_condominio.exception.ValidacionUnidadException;
import venta_condominio.model.FotoPerfilRequest;
import venta_condominio.model.Unidad;
import venta_condominio.service.AccesoService;
import venta_condominio.service.UnidadService;

@RestController
@RequestMapping("/unidades")
public class UnidadController {

    private final UnidadService unidadService;
    private final AccesoService accesoService;

    public UnidadController(UnidadService unidadService, AccesoService accesoService) {
        this.unidadService = unidadService;
        this.accesoService = accesoService;
    }

    @GetMapping
    public ResponseEntity<List<Unidad>> listarTodas() {
        return ResponseEntity.ok(unidadService.listarTodas());
    }

    /** R001: solo usuarios con sesión (y clientes con registro completo) ven las unidades. */
    @GetMapping("/disponibles")
    public ResponseEntity<List<Unidad>> listarDisponibles(@RequestParam(required = false) String email) {
        accesoService.exigirPuedeVerUnidades(email);
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

    @PostMapping("/{identificador}/fotos")
    public ResponseEntity<?> agregarFoto(
            @PathVariable String identificador,
            @RequestBody FotoPerfilRequest request) {

        try {
            Unidad actualizada = unidadService.agregarFoto(identificador, request.getFoto());

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Foto agregada correctamente",
                    "identificador", actualizada.getIdentificador(),
                    "fotos", actualizada.getFotos()
            ));

        } catch (ValidacionUnidadException e) {

            return ResponseEntity.badRequest().body(
                    Map.of("mensaje", e.getMessage())
            );
        }
    }

    @DeleteMapping("/{identificador}")
    public ResponseEntity<?> eliminarUnidad(@PathVariable String identificador) {

        try {
            unidadService.eliminarUnidad(identificador);

            return ResponseEntity.ok(
                    Map.of("mensaje", "Unidad eliminada correctamente")
            );

        } catch (ValidacionUnidadException e) {

            return ResponseEntity.status(404).body(
                    Map.of("mensaje", e.getMessage())
            );
        }
    }

    @PutMapping("/{identificador}")
    public ResponseEntity<?> actualizarUnidad(
            @PathVariable String identificador,
            @RequestBody Unidad unidad) {

        try {
            Unidad actualizada = unidadService.actualizarUnidad(identificador, unidad);

            return ResponseEntity.ok(actualizada);

        } catch (ValidacionUnidadException e) {

            return ResponseEntity.badRequest().body(
                    Map.of("mensaje", e.getMessage())
            );
        }
    }
}
