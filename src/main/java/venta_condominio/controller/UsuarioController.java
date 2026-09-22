package venta_condominio.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import venta_condominio.exception.UsuarioException;
import venta_condominio.model.FotoPerfilRequest;
import venta_condominio.model.LoginRequest;
import venta_condominio.model.Usuario;
import venta_condominio.service.UsuarioService;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "http://localhost:5173")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<String> registrarUsuario(
            @RequestBody Usuario usuario) {

        usuarioService.registrarUsuario(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getContrasena(),
                usuario.getRol()
        );

        return ResponseEntity.ok("Usuario registrado correctamente");
    }

    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@RequestBody LoginRequest request) {

        try {
            Usuario usuario = usuarioService.iniciarSesion(
                    request.getEmail(),
                    request.getContrasena()
            );

            return ResponseEntity.ok(respuestaUsuario(
                    usuario, "Inicio de sesión exitoso"
            ));

        } catch (UsuarioException e) {

            return ResponseEntity.status(401).body(
                    Map.of("mensaje", e.getMessage())
            );
        }
    }

    @PutMapping("/{id}/foto")
    public ResponseEntity<?> actualizarFotoPerfil(
            @PathVariable String id,
            @RequestBody FotoPerfilRequest request) {

        try {
            Usuario actualizado = usuarioService.actualizarFotoPerfil(
                    id, request.getFoto()
            );

            return ResponseEntity.ok(respuestaUsuario(
                    actualizado, "Foto de perfil actualizada"
            ));

        } catch (UsuarioException e) {

            return ResponseEntity.badRequest().body(
                    Map.of("mensaje", e.getMessage())
            );
        }
    }

    private Map<String, Object> respuestaUsuario(Usuario usuario, String mensaje) {

        return Map.of(
                "mensaje", mensaje,
                "id", usuario.getId(),
                "nombre", usuario.getNombre(),
                "email", usuario.getEmail(),
                "rol", usuario.getRol().toString(),
                "fotoPerfil", usuario.getFotoPerfil() != null ? usuario.getFotoPerfil() : ""
        );
    }
}