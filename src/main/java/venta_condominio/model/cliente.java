package venta_condominio.model;

import java.math.BigDecimal;

import venta_condominio.exception.ValidacionClienteException;

public class Cliente {

    private final String nombre;
    private final String primerApellido;
    private final String segundoApellido;
    private final String email;
    private final String telefono;
    private final BigDecimal salario;
    private final BigDecimal presupuesto;
    private final Departamento departamento;
    private final Municipio municipio;

    public Cliente(
            String nombre,
            String primerApellido,
            String segundoApellido,
            String email,
            String telefono,
            BigDecimal salario,
            BigDecimal presupuesto,
            Departamento departamento,
            Municipio municipio) {

        validarDatos(
                nombre,
                primerApellido,
                segundoApellido,
                email,
                telefono,
                salario,
                presupuesto,
                departamento,
                municipio
        );

        this.nombre = nombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.email = email;
        this.telefono = telefono;
        this.salario = salario;
        this.presupuesto = presupuesto;
        this.departamento = departamento;
        this.municipio = municipio;
    }

    private void validarDatos(
            String nombre,
            String primerApellido,
            String segundoApellido,
            String email,
            String telefono,
            BigDecimal salario,
            BigDecimal presupuesto,
            Departamento departamento,
            Municipio municipio) {

        if (nombre == null || nombre.isBlank()) {
            throw new ValidacionClienteException(
                    "El nombre es obligatorio"
            );
        }

        if (primerApellido == null || primerApellido.isBlank()) {
            throw new ValidacionClienteException(
                    "El primer apellido es obligatorio"
            );
        }

        if (segundoApellido == null || segundoApellido.isBlank()) {
            throw new ValidacionClienteException(
                    "El segundo apellido es obligatorio"
            );
        }

        if (email == null || email.isBlank()) {
            throw new ValidacionClienteException(
                    "El correo electrónico es obligatorio"
            );
        }

        if (telefono == null || telefono.isBlank()) {
            throw new ValidacionClienteException(
                    "El teléfono es obligatorio"
            );
        }

        if (telefono.length() != 10) {
            throw new ValidacionClienteException(
                    "El teléfono debe tener exactamente 10 caracteres"
            );
        }

        if (salario == null || salario.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacionClienteException(
                    "El salario no puede ser menor que cero"
            );
        }

        if (presupuesto == null || presupuesto.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacionClienteException(
                    "El presupuesto no puede ser menor que cero"
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
    }
}