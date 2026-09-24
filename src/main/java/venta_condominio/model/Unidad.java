package venta_condominio.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import venta_condominio.exception.ValidacionUnidadException;

public class Unidad {

    private final String identificador;
    private final String tipoInmueble;
    private final BigDecimal precioLista;
    private final Integer area;
    private final Integer habitaciones;
    private final Integer banos;
    private final String descripcion;
    private final Departamento departamento;
    private final Municipio municipio;
    private final EstadoUnidad estado;
    private final List<String> fotos;

    @JsonCreator
    public Unidad(
            @JsonProperty("identificador") String identificador,
            @JsonProperty("tipoInmueble") String tipoInmueble,
            @JsonProperty("precioLista") BigDecimal precioLista,
            @JsonProperty("area") Integer area,
            @JsonProperty("habitaciones") Integer habitaciones,
            @JsonProperty("banos") Integer banos,
            @JsonProperty("descripcion") String descripcion,
            @JsonProperty("departamento") Departamento departamento,
            @JsonProperty("municipio") Municipio municipio,
            @JsonProperty("estado") EstadoUnidad estado,
            @JsonProperty("fotos") List<String> fotos) {

        if (identificador == null || identificador.isBlank()) {
            throw new ValidacionUnidadException("El identificador de la unidad es obligatorio");
        }

        if (tipoInmueble == null || tipoInmueble.isBlank()) {
            throw new ValidacionUnidadException("El tipo de inmueble es obligatorio");
        }

        if (precioLista == null || precioLista.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacionUnidadException("El precio de lista debe ser mayor a cero");
        }

        if (area == null || area <= 0) {
            throw new ValidacionUnidadException("El área debe ser mayor a cero");
        }

        if (departamento == null) {
            throw new ValidacionUnidadException("El departamento es obligatorio");
        }

        if (municipio == null) {
            throw new ValidacionUnidadException("El municipio es obligatorio");
        }

        this.identificador = identificador;
        this.tipoInmueble = tipoInmueble;
        this.precioLista = precioLista;
        this.area = area;
        this.habitaciones = habitaciones;
        this.banos = banos;
        this.descripcion = descripcion;
        this.departamento = departamento;
        this.municipio = municipio;
        this.estado = estado != null ? estado : EstadoUnidad.DISPONIBLE;
        this.fotos = fotos != null ? new ArrayList<>(fotos) : new ArrayList<>();
    }

    public String getIdentificador() {
        return identificador;
    }

    public String getTipoInmueble() {
        return tipoInmueble;
    }

    public BigDecimal getPrecioLista() {
        return precioLista;
    }

    public Integer getArea() {
        return area;
    }

    public Integer getHabitaciones() {
        return habitaciones;
    }

    public Integer getBanos() {
        return banos;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public Municipio getMunicipio() {
        return municipio;
    }

    public EstadoUnidad getEstado() {
        return estado;
    }

    public List<String> getFotos() {
        return fotos;
    }

    public Unidad conEstado(EstadoUnidad nuevoEstado) {
        return new Unidad(
                identificador, tipoInmueble, precioLista, area,
                habitaciones, banos, descripcion, departamento,
                municipio, nuevoEstado, fotos
        );
    }

    public Unidad conFotoAgregada(String nuevaFotoBase64) {
        List<String> fotosActualizadas = new ArrayList<>(fotos);
        fotosActualizadas.add(nuevaFotoBase64);

        return new Unidad(
                identificador, tipoInmueble, precioLista, area,
                habitaciones, banos, descripcion, departamento,
                municipio, estado, fotosActualizadas
        );
    }
}