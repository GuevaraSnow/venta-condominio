package venta_condominio.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Usuario {

    private final String id;
    private final String nombre;
    private final String email;
    private final String contrasena;
    private final Rol rol;
    private final boolean activo;
    private final String fotoPerfil;
    private final boolean aceptoTratamientoDatos;
    private final String fechaAceptacionDatos;

    public Usuario(
            String id,
            String nombre,
            String email,
            String contrasena,
            Rol rol,
            boolean activo,
            String fotoPerfil) {

        this(id, nombre, email, contrasena, rol, activo, fotoPerfil, false, null);
    }

    @JsonCreator
    public Usuario(
            @JsonProperty("id") String id,
            @JsonProperty("nombre") String nombre,
            @JsonProperty("email") String email,
            @JsonProperty("contrasena") String contrasena,
            @JsonProperty("rol") Rol rol,
            @JsonProperty("activo") boolean activo,
            @JsonProperty("fotoPerfil") String fotoPerfil,
            @JsonProperty("aceptoTratamientoDatos") boolean aceptoTratamientoDatos,
            @JsonProperty("fechaAceptacionDatos") String fechaAceptacionDatos) {

        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.contrasena = contrasena;
        this.rol = rol;
        this.activo = activo;
        this.fotoPerfil = fotoPerfil;
        this.aceptoTratamientoDatos = aceptoTratamientoDatos;
        this.fechaAceptacionDatos = fechaAceptacionDatos;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getContrasena() {
        return contrasena;
    }

    public Rol getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public boolean isAceptoTratamientoDatos() {
        return aceptoTratamientoDatos;
    }

    public String getFechaAceptacionDatos() {
        return fechaAceptacionDatos;
    }

    public Usuario conFotoPerfil(String nuevaFoto) {
        return new Usuario(id, nombre, email, contrasena, rol, activo, nuevaFoto,
                aceptoTratamientoDatos, fechaAceptacionDatos);
    }
}