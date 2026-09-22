package venta_condominio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import venta_condominio.service.VerificacionEmailService;

import java.util.Map;

@RestController
@RequestMapping("/verificacion-email")
@CrossOrigin(origins = "http://localhost:5173")
public class VerificacionEmailController {

    private final VerificacionEmailService verificacionEmailService;

    public VerificacionEmailController(
            VerificacionEmailService verificacionEmailService) {

        this.verificacionEmailService = verificacionEmailService;
    }

    @PostMapping("/generar")
    public ResponseEntity<Map<String, String>> generarCodigo(
            @RequestBody Map<String, String> datos) {

        String email = datos.get("email");

        verificacionEmailService.generarCodigo(email);

        return ResponseEntity.ok(
                Map.of(
                        "mensaje",
                        "Código de verificación enviado al correo electrónico"
                )
        );
    }

    @PostMapping("/verificar")
    public ResponseEntity<Map<String, String>> verificarCodigo(
            @RequestBody Map<String, String> datos) {

        String email = datos.get("email");
        String codigo = datos.get("codigo");

        boolean verificado =
                verificacionEmailService.verificarCodigo(
                        email,
                        codigo
                );

        if (!verificado) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "mensaje",
                            "El código de verificación es incorrecto"
                    )
            );
        }

        return ResponseEntity.ok(
                Map.of(
                        "mensaje",
                        "Correo electrónico verificado correctamente"
                )
        );
    }
}