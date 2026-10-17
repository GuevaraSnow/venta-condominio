package venta_condominio.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import venta_condominio.exception.ValidacionReservaException;

/**
 * Reserva de una unidad a partir de una cotización firmada (F-04).
 * Inmutable: los cambios de estado crean una copia con conEstado(...).
 */
public class Reserva {

    private final String id;
    private final String idCotizacion;
    private final String identificadorUnidad;
    private final String emailCliente;
    private final String nombreCliente;
    private final String creadaPor;
    private final BigDecimal precioLista;
    private final BigDecimal precioFinal;
    private final BigDecimal cuotaReserva;
    private final String fechaReserva;
    private final EstadoReserva estado;
    private final String observacion;

    @JsonCreator
    public Reserva(
            @JsonProperty("id") String id,
            @JsonProperty("idCotizacion") String idCotizacion,
            @JsonProperty("identificadorUnidad") String identificadorUnidad,
            @JsonProperty("emailCliente") String emailCliente,
            @JsonProperty("nombreCliente") String nombreCliente,
            @JsonProperty("creadaPor") String creadaPor,
            @JsonProperty("precioLista") BigDecimal precioLista,
            @JsonProperty("precioFinal") BigDecimal precioFinal,
            @JsonProperty("cuotaReserva") BigDecimal cuotaReserva,
            @JsonProperty("fechaReserva") String fechaReserva,
            @JsonProperty("estado") EstadoReserva estado,
            @JsonProperty("observacion") String observacion) {

        if (id == null || id.isBlank()) {
            throw new ValidacionReservaException("El id de la reserva es obligatorio");
        }

        if (idCotizacion == null || identificadorUnidad == null || emailCliente == null) {
            throw new ValidacionReservaException("La reserva está incompleta");
        }

        if (cuotaReserva == null || cuotaReserva.signum() <= 0 || estado == null) {
            throw new ValidacionReservaException("La reserva está incompleta");
        }

        this.id = id;
        this.idCotizacion = idCotizacion;
        this.identificadorUnidad = identificadorUnidad;
        this.emailCliente = emailCliente;
        this.nombreCliente = nombreCliente;
        this.creadaPor = creadaPor;
        this.precioLista = precioLista;
        this.precioFinal = precioFinal;
        this.cuotaReserva = cuotaReserva;
        this.fechaReserva = fechaReserva;
        this.estado = estado;
        this.observacion = observacion;
    }

    public Reserva conEstado(EstadoReserva nuevoEstado, String observacion) {
        return new Reserva(id, idCotizacion, identificadorUnidad, emailCliente, nombreCliente,
                creadaPor, precioLista, precioFinal, cuotaReserva, fechaReserva, nuevoEstado,
                observacion != null ? observacion : this.observacion);
    }

    public String getId() { return id; }
    public String getIdCotizacion() { return idCotizacion; }
    public String getIdentificadorUnidad() { return identificadorUnidad; }
    public String getEmailCliente() { return emailCliente; }
    public String getNombreCliente() { return nombreCliente; }
    public String getCreadaPor() { return creadaPor; }
    public BigDecimal getPrecioLista() { return precioLista; }
    public BigDecimal getPrecioFinal() { return precioFinal; }
    public BigDecimal getCuotaReserva() { return cuotaReserva; }
    public String getFechaReserva() { return fechaReserva; }
    public EstadoReserva getEstado() { return estado; }
    public String getObservacion() { return observacion; }
}
