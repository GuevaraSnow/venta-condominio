package venta_condominio.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import venta_condominio.util.Almacenamiento;

import java.util.concurrent.TimeUnit;

/**
 * Sirve las imágenes guardadas (fotos de perfil y de unidades),
 * vengan del disco (local) o de MongoDB (producción).
 */
@RestController
public class ArchivoController {

    @GetMapping("/archivos/{carpeta}/{nombre}")
    public ResponseEntity<byte[]> obtener(
            @PathVariable String carpeta,
            @PathVariable String nombre) {

        byte[] bytes = Almacenamiento.leerArchivo(carpeta + "/" + nombre);

        if (bytes == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(tipoDeContenido(nombre))
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                .body(bytes);
    }

    private MediaType tipoDeContenido(String nombre) {

        if (nombre.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }

        if (nombre.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }

        return MediaType.IMAGE_JPEG;
    }
}
