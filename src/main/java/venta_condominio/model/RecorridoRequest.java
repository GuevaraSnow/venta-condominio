package venta_condominio.model;

/** Datos para agendar un recorrido. "email" es quien agenda (cliente o asesor). */
public class RecorridoRequest {

    private String identificadorUnidad;
    private String emailCliente;
    private String email;
    private String fecha;
    private String hora;

    public RecorridoRequest() {
    }

    public RecorridoRequest(String identificadorUnidad, String emailCliente, String email,
                            String fecha, String hora) {
        this.identificadorUnidad = identificadorUnidad;
        this.emailCliente = emailCliente;
        this.email = email;
        this.fecha = fecha;
        this.hora = hora;
    }

    public String getIdentificadorUnidad() { return identificadorUnidad; }
    public void setIdentificadorUnidad(String identificadorUnidad) { this.identificadorUnidad = identificadorUnidad; }
    public String getEmailCliente() { return emailCliente; }
    public void setEmailCliente(String emailCliente) { this.emailCliente = emailCliente; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }
}
