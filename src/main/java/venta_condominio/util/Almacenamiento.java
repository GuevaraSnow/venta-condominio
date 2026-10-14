package venta_condominio.util;

import java.io.File;

/**
 * Centraliza dónde se guardan los archivos de datos (JSON e imágenes).
 *
 * En local no hay que configurar nada: se usa la carpeta del proyecto,
 * igual que antes. En el servidor se define la variable de entorno
 * DATA_DIR apuntando al volumen persistente (por ejemplo /data), para
 * que los datos no se borren en cada despliegue.
 */
public final class Almacenamiento {

    private Almacenamiento() {
    }

    public static File directorioBase() {

        String dataDir = System.getenv("DATA_DIR");

        File base = (dataDir == null || dataDir.isBlank())
                ? new File(".")
                : new File(dataDir);

        if (!base.exists()) {
            base.mkdirs();
        }

        return base;
    }

    public static File archivo(String nombre) {
        return new File(directorioBase(), nombre);
    }

    public static File directorioUploads() {
        return new File(directorioBase(), "uploads");
    }
}
