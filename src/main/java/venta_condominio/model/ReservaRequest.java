package venta_condominio.model;

/** Datos para reservar: la cotización firmada y quién hace la reserva. */
public class ReservaRequest {

    private String idCotizacion;
    private String email;

    public ReservaRequest() {
    }

    public ReservaRequest(String idCotizacion, String email) {
        this.idCotizacion = idCotizacion;
        this.email = email;
    }

    public String getIdCotizacion() {
        return idCotizacion;
    }

    public void setIdCotizacion(String idCotizacion) {
        this.idCotizacion = idCotizacion;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
