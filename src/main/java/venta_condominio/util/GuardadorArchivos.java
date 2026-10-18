package venta_condominio.util;

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

        Almacenamiento.guardarArchivo(carpeta + "/" + nombreArchivo, bytes);

        // Ruta relativa: el frontend le antepone la URL del backend
        // (VITE_API_URL), así funciona igual en local y en producción.
        return "/archivos/" + carpeta + "/" + nombreArchivo;
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

        // GIF u otros formatos no se aceptan (las fotos de perfil no deben ser animadas)
        throw new IllegalArgumentException("Solo se permiten imágenes JPG, PNG o WEBP");
    }
}
