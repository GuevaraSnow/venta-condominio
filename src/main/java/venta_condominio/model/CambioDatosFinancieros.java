package venta_condominio.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Registro de auditoría: cada vez que el director comercial cambia el salario
 * o el presupuesto de un cliente queda quién, cuándo, qué valores había antes,
 * cuáles quedaron y el soporte que lo justifica.
 */
public class CambioDatosFinancieros {

    private final String id;
    private final String emailCliente;
    private final BigDecimal salarioAnterior;
    private final BigDecimal salarioNuevo;
    private final BigDecimal presupuestoAnterior;
    private final BigDecimal presupuestoNuevo;
    private final String modificadoPor;
    private final String fecha;
    private final String motivo;

    @JsonCreator
    public CambioDatosFinancieros(
            @JsonProperty("id") String id,
            @JsonProperty("emailCliente") String emailCliente,
            @JsonProperty("salarioAnterior") BigDecimal salarioAnterior,
            @JsonProperty("salarioNuevo") BigDecimal salarioNuevo,
            @JsonProperty("presupuestoAnterior") BigDecimal presupuestoAnterior,
            @JsonProperty("presupuestoNuevo") BigDecimal presupuestoNuevo,
            @JsonProperty("modificadoPor") String modificadoPor,
            @JsonProperty("fecha") String fecha,
            @JsonProperty("motivo") String motivo) {
        this.id = id;
        this.emailCliente = emailCliente;
        this.salarioAnterior = salarioAnterior;
        this.salarioNuevo = salarioNuevo;
        this.presupuestoAnterior = presupuestoAnterior;
        this.presupuestoNuevo = presupuestoNuevo;
        this.modificadoPor = modificadoPor;
        this.fecha = fecha;
        this.motivo = motivo;
    }

    public String getId() { return id; }
    public String getEmailCliente() { return emailCliente; }
    public BigDecimal getSalarioAnterior() { return salarioAnterior; }
    public BigDecimal getSalarioNuevo() { return salarioNuevo; }
    public BigDecimal getPresupuestoAnterior() { return presupuestoAnterior; }
    public BigDecimal getPresupuestoNuevo() { return presupuestoNuevo; }
    public String getModificadoPor() { return modificadoPor; }
    public String getFecha() { return fecha; }
    public String getMotivo() { return motivo; }
}
