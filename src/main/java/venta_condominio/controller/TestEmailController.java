package venta_condominio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import venta_condominio.service.ServicioEnvioEmail;

@RestController
public class TestEmailController {

    private final ServicioEnvioEmail servicioEnvioEmail;

    public TestEmailController(ServicioEnvioEmail servicioEnvioEmail) {
        this.servicioEnvioEmail = servicioEnvioEmail;
    }

    @GetMapping("/test-email")
    public ResponseEntity<String> enviarCorreoPrueba() {

        servicioEnvioEmail.enviarNotificacion(
                "santiguevar420@gmail.com",
                "Prueba Venta Condominio",
                "Este es un correo de prueba enviado desde Venta Condominio."
        );

        return ResponseEntity.ok("Correo enviado correctamente");
    }
}