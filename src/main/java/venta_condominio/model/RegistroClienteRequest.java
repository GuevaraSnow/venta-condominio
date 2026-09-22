package venta_condominio.model;

import java.math.BigDecimal;

public class RegistroClienteRequest {

    private String nombre;
    private String primerApellido;
    private String segundoApellido;
    private String email;
    private String telefono;
    private String contrasena;
    private BigDecimal salario;
    private BigDecimal presupuesto;
    private Departamento departamento;
    private Municipio municipio;

    public RegistroClienteRequest() {
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

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getContrasena() {
        return contrasena;
    }

    public BigDecimal getSalario() {
        return salario;
    }

    public BigDecimal getPresupuesto() {
        return presupuesto;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public Municipio getMunicipio() {
        return municipio;
    }
}