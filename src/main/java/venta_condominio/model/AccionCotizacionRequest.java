package venta_condominio.model;

/**
 * Identifica a quien autoriza, rechaza o firma una cotización
 * (director comercial o cliente).
 */
public class AccionCotizacionRequest {

    private String email;
    private String observacion;

    public AccionCotizacionRequest() {
    }

    public AccionCotizacionRequest(String email, String observacion) {
        this.email = email;
        this.observacion = observacion;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}
