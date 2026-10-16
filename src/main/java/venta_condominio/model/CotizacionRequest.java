package venta_condominio.model;

import java.math.BigDecimal;

/** Datos que envía el asesor para generar una cotización. */
public class CotizacionRequest {

    private String identificadorUnidad;
    private String emailCliente;
    private String emailAsesor;
    private BigDecimal porcentajeDescuento;

    public CotizacionRequest() {
    }

    public CotizacionRequest(String identificadorUnidad, String emailCliente,
                             String emailAsesor, BigDecimal porcentajeDescuento) {
        this.identificadorUnidad = identificadorUnidad;
        this.emailCliente = emailCliente;
        this.emailAsesor = emailAsesor;
        this.porcentajeDescuento = porcentajeDescuento;
    }

    public String getIdentificadorUnidad() {
        return identificadorUnidad;
    }

    public void setIdentificadorUnidad(String identificadorUnidad) {
        this.identificadorUnidad = identificadorUnidad;
    }

    public String getEmailCliente() {
        return emailCliente;
    }

    public void setEmailCliente(String emailCliente) {
        this.emailCliente = emailCliente;
    }

    public String getEmailAsesor() {
        return emailAsesor;
    }

    public void setEmailAsesor(String emailAsesor) {
        this.emailAsesor = emailAsesor;
    }

    public BigDecimal getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public void setPorcentajeDescuento(BigDecimal porcentajeDescuento) {
        this.porcentajeDescuento = porcentajeDescuento;
    }
}
