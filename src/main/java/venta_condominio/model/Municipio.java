package venta_condominio.model;

public class Municipio {

    private final String nombre;
    private final Departamento departamento;

    public Municipio(String nombre, Departamento departamento) {
        this.nombre = nombre;
        this.departamento = departamento;
    }

    public String getNombre() {
        return nombre;
    }

    public Departamento getDepartamento() {
        return departamento;
    }
}