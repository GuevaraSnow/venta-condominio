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

    @JsonCreator
    public Usuario(
            @JsonProperty("id") String id,
            @JsonProperty("nombre") String nombre,
            @JsonProperty("email") String email,
            @JsonProperty("contrasena") String contrasena,
            @JsonProperty("rol") Rol rol,
            @JsonProperty("activo") boolean activo,
            @JsonProperty("fotoPerfil") String fotoPerfil) {

        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.contrasena = contrasena;
        this.rol = rol;
        this.activo = activo;
        this.fotoPerfil = fotoPerfil;
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

    public Usuario conFotoPerfil(String nuevaFoto) {
        return new Usuario(id, nombre, email, contrasena, rol, activo, nuevaFoto);
    }
}