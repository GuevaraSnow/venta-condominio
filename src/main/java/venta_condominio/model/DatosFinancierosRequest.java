package venta_condominio.model;

import java.math.BigDecimal;

/** Lo que envía el asesor para actualizar salario y presupuesto de un cliente. */
public class DatosFinancierosRequest {

    private String emailAsesor;
    private BigDecimal salario;
    private BigDecimal presupuesto;
    private String motivo;

    public String getEmailAsesor() { return emailAsesor; }
    public void setEmailAsesor(String emailAsesor) { this.emailAsesor = emailAsesor; }
    public BigDecimal getSalario() { return salario; }
    public void setSalario(BigDecimal salario) { this.salario = salario; }
    public BigDecimal getPresupuesto() { return presupuesto; }
    public void setPresupuesto(BigDecimal presupuesto) { this.presupuesto = presupuesto; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
