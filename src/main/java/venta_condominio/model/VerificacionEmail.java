package venta_condominio.model;

import java.time.LocalDateTime;

public class VerificacionEmail {

    private final String email;
    private final String codigo;
    private final LocalDateTime fechaCreacion;
    private final boolean verificado;

    public VerificacionEmail(
            String email,
            String codigo,
            LocalDateTime fechaCreacion,
            boolean verificado) {

        this.email = email;
        this.codigo = codigo;
        this.fechaCreacion = fechaCreacion;
        this.verificado = verificado;
    }

    public String getEmail() {
        return email;
    }

    public String getCodigo() {
        return codigo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public boolean isVerificado() {
        return verificado;
    }
}