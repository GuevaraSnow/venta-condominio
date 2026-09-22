package venta_condominio.model;

import java.math.BigDecimal;

public class RegistroClienteRequest {

    private String nombre;
    private String primerApellido;
    private String segundoApellido;
    private String cedula;
    private String email;
    private String telefono;
    private String contrasena;
    private BigDecimal salario;
    private BigDecimal presupuesto;
    private String tipoInmueble;
    private Departamento departamento;
    private String municipio;

    public RegistroClienteRequest() {}

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

    public String getContrasena() {
        return contrasena;
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

    public String getMunicipio() {
        return municipio;
    }
}