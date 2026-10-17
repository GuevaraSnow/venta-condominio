package venta_condominio.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import venta_condominio.exception.ValidacionRecorridoException;

/** Visita de un cliente a una unidad (F-02). Inmutable. */
public class Recorrido {

    private final String id;
    private final String identificadorUnidad;
    private final String emailCliente;
    private final String nombreCliente;
    private final String agendadoPor;
    private final String fecha;
    private final String hora;
    private final EstadoRecorrido estado;
    private final String atendidoPor;

    @JsonCreator
    public Recorrido(
            @JsonProperty("id") String id,
            @JsonProperty("identificadorUnidad") String identificadorUnidad,
            @JsonProperty("emailCliente") String emailCliente,
            @JsonProperty("nombreCliente") String nombreCliente,
            @JsonProperty("agendadoPor") String agendadoPor,
            @JsonProperty("fecha") String fecha,
            @JsonProperty("hora") String hora,
            @JsonProperty("estado") EstadoRecorrido estado,
            @JsonProperty("atendidoPor") String atendidoPor) {

        if (id == null || identificadorUnidad == null || emailCliente == null
                || fecha == null || hora == null || estado == null) {
            throw new ValidacionRecorridoException("El recorrido está incompleto");
        }

        this.id = id;
        this.identificadorUnidad = identificadorUnidad;
        this.emailCliente = emailCliente;
        this.nombreCliente = nombreCliente;
        this.agendadoPor = agendadoPor;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
        this.atendidoPor = atendidoPor;
    }

    public Recorrido conEstado(EstadoRecorrido nuevoEstado, String atendidoPor) {
        return new Recorrido(id, identificadorUnidad, emailCliente, nombreCliente, agendadoPor,
                fecha, hora, nuevoEstado, atendidoPor != null ? atendidoPor : this.atendidoPor);
    }

    public String getId() { return id; }
    public String getIdentificadorUnidad() { return identificadorUnidad; }
    public String getEmailCliente() { return emailCliente; }
    public String getNombreCliente() { return nombreCliente; }
    public String getAgendadoPor() { return agendadoPor; }
    public String getFecha() { return fecha; }
    public String getHora() { return hora; }
    public EstadoRecorrido getEstado() { return estado; }
    public String getAtendidoPor() { return atendidoPor; }
}
