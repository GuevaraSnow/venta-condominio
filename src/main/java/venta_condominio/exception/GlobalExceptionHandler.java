package venta_condominio.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsuarioException.class)
    public ResponseEntity<Map<String, String>> manejarUsuarioException(
            UsuarioException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje", exception.getMessage()));
    }

    @ExceptionHandler(ValidacionClienteException.class)
    public ResponseEntity<Map<String, String>> manejarValidacionClienteException(
            ValidacionClienteException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje", exception.getMessage()));
    }

    @ExceptionHandler(ValidacionUnidadException.class)
    public ResponseEntity<Map<String, String>> manejarValidacionUnidadException(
            ValidacionUnidadException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje", exception.getMessage()));
    }

    @ExceptionHandler(ValidacionCotizacionException.class)
    public ResponseEntity<Map<String, String>> manejarValidacionCotizacionException(
            ValidacionCotizacionException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje", exception.getMessage()));
    }

    @ExceptionHandler(ValidacionReservaException.class)
    public ResponseEntity<Map<String, String>> manejarValidacionReservaException(
            ValidacionReservaException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje", exception.getMessage()));
    }

    @ExceptionHandler(ValidacionRecorridoException.class)
    public ResponseEntity<Map<String, String>> manejarValidacionRecorridoException(
            ValidacionRecorridoException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje", exception.getMessage()));
    }

    /** Formato de imagen no permitido (GIF, etc.) y otros datos inválidos. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarIllegalArgumentException(
            IllegalArgumentException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje", exception.getMessage()));
    }

    /** R001: sin sesión o sin registro completo no se ven unidades. */
    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<Map<String, String>> manejarAccesoDenegadoException(
            AccesoDenegadoException exception) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of("mensaje", exception.getMessage()));
    }
}
