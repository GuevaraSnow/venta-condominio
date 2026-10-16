package venta_condominio.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import venta_condominio.service.RecuperacionContrasenaService;

@RestController
@RequestMapping("/usuarios")
public class RecuperacionContrasenaController {

    private final RecuperacionContrasenaService recuperacionService;

    public RecuperacionContrasenaController(RecuperacionContrasenaService recuperacionService) {
        this.recuperacionService = recuperacionService;
    }

    /** Recibe { "email" } y envía el código si el correo está registrado. */
    @PostMapping("/recuperar-contrasena")
    public ResponseEntity<Map<String, String>> solicitarCodigo(
            @RequestBody Map<String, String> datos) {

        recuperacionService.solicitarCodigo(datos.get("email"));

        return ResponseEntity.ok(Map.of(
                "mensaje",
                "Si el correo está registrado, te enviamos un código para restablecer la contraseña"
        ));
    }

    /** Recibe { "email", "codigo", "nuevaContrasena" }. */
    @PostMapping("/restablecer-contrasena")
    public ResponseEntity<Map<String, String>> restablecer(
            @RequestBody Map<String, String> datos) {

        recuperacionService.restablecerContrasena(
                datos.get("email"),
                datos.get("codigo"),
                datos.get("nuevaContrasena")
        );

        return ResponseEntity.ok(Map.of(
                "mensaje", "Contraseña actualizada. Ya puedes iniciar sesión."
        ));
    }
}
