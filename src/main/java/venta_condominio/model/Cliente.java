package venta_condominio.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import venta_condominio.exception.ValidacionClienteException;

public class Cliente {

    private final String nombre;
    private final String primerApellido;
    private final String segundoApellido;
    private final String cedula;
    private final String email;
    private final String telefono;
    private final BigDecimal salario;
    private final BigDecimal presupuesto;
    private final String tipoInmueble;
    private final Departamento departamento;
    private final Municipio municipio;

    @JsonCreator
    public Cliente(
            @JsonProperty("nombre") String nombre,
            @JsonProperty("primerApellido") String primerApellido,
            @JsonProperty("segundoApellido") String segundoApellido,
            @JsonProperty("cedula") String cedula,
            @JsonProperty("email") String email,
            @JsonProperty("telefono") String telefono,
            @JsonProperty("salario") BigDecimal salario,
            @JsonProperty("presupuesto") BigDecimal presupuesto,
            @JsonProperty("tipoInmueble") String tipoInmueble,
            @JsonProperty("departamento") Departamento departamento,
            @JsonProperty("municipio") Municipio municipio) {

        if (nombre == null || nombre.isBlank()) {
            throw new ValidacionClienteException("El nombre es obligatorio");
        }

        if (primerApellido == null || primerApellido.isBlank()) {
            throw new ValidacionClienteException("El primer apellido es obligatorio");
        }

        if (segundoApellido == null || segundoApellido.isBlank()) {
            throw new ValidacionClienteException("El segundo apellido es obligatorio");
        }

        if (cedula == null || cedula.isBlank()) {
            throw new ValidacionClienteException("La cédula es obligatoria");
        }

        if (email == null || email.isBlank()) {
            throw new ValidacionClienteException("El correo electrónico es obligatorio");
        }

        if (telefono == null || telefono.isBlank()) {
            throw new ValidacionClienteException("El teléfono es obligatorio");
        }

        if (telefono.length() != 10) {
            throw new ValidacionClienteException(
                    "El teléfono debe tener 10 dígitos"
            );
        }

        if (salario == null || salario.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacionClienteException(
                    "El salario no puede ser negativo"
            );
        }

        if (presupuesto == null || presupuesto.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacionClienteException(
                    "El presupuesto no puede ser negativo"
            );
        }

        if (tipoInmueble == null || tipoInmueble.isBlank()) {
            throw new ValidacionClienteException(
                    "El tipo de inmueble es obligatorio"
            );
        }

        if (departamento == null) {
            throw new ValidacionClienteException(
                    "El departamento es obligatorio"
            );
        }

        if (municipio == null) {
            throw new ValidacionClienteException(
                    "El municipio es obligatorio"
            );
        }

        this.nombre = nombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.cedula = cedula;
        this.email = email;
        this.telefono = telefono;
        this.salario = salario;
        this.presupuesto = presupuesto;
        this.tipoInmueble = tipoInmueble;
        this.departamento = departamento;
        this.municipio = municipio;
    }

    public String getNombre() {
        return nombre;
    }

    public String getPrimerApellido() {
        return primerApellido;
    }

    public String getSegundoApellido() {
        return segundoApellido;
    }

    public String getCedula() {
        return cedula;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }

    public BigDecimal getSalario() {
        return salario;
    }

    public BigDecimal getPresupuesto() {
        return presupuesto;
    }

    public String getTipoInmueble() {
        return tipoInmueble;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public Municipio getMunicipio() {
        return municipio;
    }
}