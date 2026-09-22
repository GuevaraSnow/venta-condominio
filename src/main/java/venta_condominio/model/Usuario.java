package venta_condominio.model;

public class Usuario {

    private final String id;
    private final String nombre;
    private final String email;
    private final String contrasena;
    private final Rol rol;
    private final boolean activo;

    public Usuario(
            String id,
            String nombre,
            String email,
            String contrasena,
            Rol rol,
            boolean activo) {

        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.contrasena = contrasena;
        this.rol = rol;
        this.activo = activo;
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
}