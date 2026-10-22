package venta_condominio.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import venta_condominio.exception.ValidacionCotizacionException;

/**
 * Cotización de una unidad para un cliente (F-03).
 * Es inmutable: cada cambio de estado crea una copia con conEstado(...).
 * Las fechas se guardan como texto ISO (aaaa-mm-dd) en el JSON.
 */
public class Cotizacion {

    private final String id;
    private final String identificadorUnidad;
    private final String emailCliente;
    private final String nombreCliente;
    private final String emailAsesor;
    private final BigDecimal precioLista;
    private final BigDecimal porcentajeDescuento;
    private final BigDecimal valorDescuento;
    private final BigDecimal precioFinal;
    private final BigDecimal cuotaInicial;
    private final BigDecimal saldoFinanciar;
    private final String fechaCreacion;
    private final String fechaVencimiento;
    private final EstadoCotizacion estado;
    private final String gestionadoPor;
    private final String observacion;

    // "Foto" de la precalificación: con qué cifras del cliente se aprobó la cotización.
    // Si después cambian los datos del cliente, esto queda igual como evidencia.
    private final BigDecimal presupuestoCliente;
    private final BigDecimal salarioCliente;
    private final BigDecimal cuotaMensualEstimada;
    private final BigDecimal salarioMinimoRequerido;

    /** Constructor sin la foto de precalificación (cotizaciones antiguas y pruebas). */
    public Cotizacion(String id, String identificadorUnidad, String emailCliente, String nombreCliente,
                      String emailAsesor, BigDecimal precioLista, BigDecimal porcentajeDescuento,
                      BigDecimal valorDescuento, BigDecimal precioFinal, BigDecimal cuotaInicial,
                      BigDecimal saldoFinanciar, String fechaCreacion, String fechaVencimiento,
                      EstadoCotizacion estado, String gestionadoPor, String observacion) {
        this(id, identificadorUnidad, emailCliente, nombreCliente, emailAsesor, precioLista,
                porcentajeDescuento, valorDescuento, precioFinal, cuotaInicial, saldoFinanciar,
                fechaCreacion, fechaVencimiento, estado, gestionadoPor, observacion,
                null, null, null, null);
    }

    @JsonCreator
    public Cotizacion(
            @JsonProperty("id") String id,
            @JsonProperty("identificadorUnidad") String identificadorUnidad,
            @JsonProperty("emailCliente") String emailCliente,
            @JsonProperty("nombreCliente") String nombreCliente,
            @JsonProperty("emailAsesor") String emailAsesor,
            @JsonProperty("precioLista") BigDecimal precioLista,
            @JsonProperty("porcentajeDescuento") BigDecimal porcentajeDescuento,
            @JsonProperty("valorDescuento") BigDecimal valorDescuento,
            @JsonProperty("precioFinal") BigDecimal precioFinal,
            @JsonProperty("cuotaInicial") BigDecimal cuotaInicial,
            @JsonProperty("saldoFinanciar") BigDecimal saldoFinanciar,
            @JsonProperty("fechaCreacion") String fechaCreacion,
            @JsonProperty("fechaVencimiento") String fechaVencimiento,
            @JsonProperty("estado") EstadoCotizacion estado,
            @JsonProperty("gestionadoPor") String gestionadoPor,
            @JsonProperty("observacion") String observacion,
            @JsonProperty("presupuestoCliente") BigDecimal presupuestoCliente,
            @JsonProperty("salarioCliente") BigDecimal salarioCliente,
            @JsonProperty("cuotaMensualEstimada") BigDecimal cuotaMensualEstimada,
            @JsonProperty("salarioMinimoRequerido") BigDecimal salarioMinimoRequerido) {

        if (id == null || id.isBlank()) {
            throw new ValidacionCotizacionException("El id de la cotización es obligatorio");
        }

        if (identificadorUnidad == null || identificadorUnidad.isBlank()) {
            throw new ValidacionCotizacionException("La unidad es obligatoria");
        }

        if (emailCliente == null || emailCliente.isBlank()) {
            throw new ValidacionCotizacionException("El cliente es obligatorio");
        }

        if (precioLista == null || precioFinal == null || estado == null) {
            throw new ValidacionCotizacionException("La cotización está incompleta");
        }

        this.id = id;
        this.identificadorUnidad = identificadorUnidad;
        this.emailCliente = emailCliente;
        this.nombreCliente = nombreCliente;
        this.emailAsesor = emailAsesor;
        this.precioLista = precioLista;
        this.porcentajeDescuento = porcentajeDescuento;
        this.valorDescuento = valorDescuento;
        this.precioFinal = precioFinal;
        this.cuotaInicial = cuotaInicial;
        this.saldoFinanciar = saldoFinanciar;
        this.fechaCreacion = fechaCreacion;
        this.fechaVencimiento = fechaVencimiento;
        this.estado = estado;
        this.gestionadoPor = gestionadoPor;
        this.observacion = observacion;
        this.presupuestoCliente = presupuestoCliente;
        this.salarioCliente = salarioCliente;
        this.cuotaMensualEstimada = cuotaMensualEstimada;
        this.salarioMinimoRequerido = salarioMinimoRequerido;
    }

    /** Copia de la cotización con otro estado. */
    public Cotizacion conEstado(EstadoCotizacion nuevoEstado, String gestionadoPor, String observacion) {
        return new Cotizacion(id, identificadorUnidad, emailCliente, nombreCliente, emailAsesor,
                precioLista, porcentajeDescuento, valorDescuento, precioFinal, cuotaInicial,
                saldoFinanciar, fechaCreacion, fechaVencimiento, nuevoEstado,
                gestionadoPor != null ? gestionadoPor : this.gestionadoPor,
                observacion != null ? observacion : this.observacion,
                presupuestoCliente, salarioCliente, cuotaMensualEstimada, salarioMinimoRequerido);
    }

    /** Vencida = todavía no firmada ni cerrada y ya pasó su fecha de vencimiento. */
    @JsonIgnore
    public boolean estaVencida(LocalDate hoy) {
        boolean abierta = estado == EstadoCotizacion.GENERADA
                || estado == EstadoCotizacion.PENDIENTE_AUTORIZACION;

        return abierta && fechaVencimiento != null
                && hoy.isAfter(LocalDate.parse(fechaVencimiento));
    }

    public String getId() { return id; }
    public String getIdentificadorUnidad() { return identificadorUnidad; }
    public String getEmailCliente() { return emailCliente; }
    public String getNombreCliente() { return nombreCliente; }
    public String getEmailAsesor() { return emailAsesor; }
    public BigDecimal getPrecioLista() { return precioLista; }
    public BigDecimal getPorcentajeDescuento() { return porcentajeDescuento; }
    public BigDecimal getValorDescuento() { return valorDescuento; }
    public BigDecimal getPrecioFinal() { return precioFinal; }
    public BigDecimal getCuotaInicial() { return cuotaInicial; }
    public BigDecimal getSaldoFinanciar() { return saldoFinanciar; }
    public String getFechaCreacion() { return fechaCreacion; }
    public String getFechaVencimiento() { return fechaVencimiento; }
    public EstadoCotizacion getEstado() { return estado; }
    public String getGestionadoPor() { return gestionadoPor; }
    public String getObservacion() { return observacion; }
    public BigDecimal getPresupuestoCliente() { return presupuestoCliente; }
    public BigDecimal getSalarioCliente() { return salarioCliente; }
    public BigDecimal getCuotaMensualEstimada() { return cuotaMensualEstimada; }
    public BigDecimal getSalarioMinimoRequerido() { return salarioMinimoRequerido; }
}
