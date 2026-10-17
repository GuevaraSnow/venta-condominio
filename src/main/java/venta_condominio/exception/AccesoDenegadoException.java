package venta_condominio.exception;

/** R001: el usuario no puede ver unidades (no existe o su registro está incompleto). */
public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
