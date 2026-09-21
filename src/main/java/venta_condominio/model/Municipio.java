package venta_condominio.model;

public class Municipio {

    private final String nombre;
    private final Departamento departamento;

    public Municipio(String nombre, Departamento departamento) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del municipio es obligatorio");
        }

        if (departamento == null) {
            throw new IllegalArgumentException("El departamento es obligatorio");
        }

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