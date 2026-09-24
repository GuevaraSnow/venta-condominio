package venta_condominio.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.UUID;

public class GuardadorArchivos {

    public static String guardarImagenBase64(String dataUrl, String carpeta) {

        if (dataUrl == null || !dataUrl.startsWith("data:")) {
            throw new IllegalArgumentException("La imagen debe venir como data URL (data:image/...)");
        }

        // Ejemplo de dataUrl: "data:image/jpeg;base64,/9j/4AAQ..."
        String[] partes = dataUrl.split(",", 2);
        String encabezado = partes[0]; // data:image/jpeg;base64
        String base64 = partes[1];

        String extension = extraerExtension(encabezado);

        byte[] bytes = Base64.getDecoder().decode(base64);

        String nombreArchivo = UUID.randomUUID() + "." + extension;

        try {
            File directorio = new File("uploads/" + carpeta);

            if (!directorio.exists()) {
                directorio.mkdirs();
            }

            File archivo = new File(directorio, nombreArchivo);

            Files.write(archivo.toPath(), bytes);

        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar la imagen", e);
        }

        // URL completa: el frontend corre en otro puerto (5173), así que
        // una ruta relativa no funcionaría para cargar la imagen.
        return "http://localhost:8080/archivos/" + carpeta + "/" + nombreArchivo;
    }

    private static String extraerExtension(String encabezado) {

        if (encabezado.contains("jpeg") || encabezado.contains("jpg")) {
            return "jpg";
        }

        if (encabezado.contains("png")) {
            return "png";
        }

        if (encabezado.contains("webp")) {
            return "webp";
        }

        return "jpg";
    }
}