package venta_condominio.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.bson.Document;
import org.bson.types.Binary;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;

/**
 * Centraliza dónde se guardan los datos (los JSON y las imágenes).
 *
 * - Sin configurar nada (en local): se guardan como archivos en la carpeta
 *   del proyecto, igual que siempre. Con DATA_DIR se cambia la carpeta.
 * - Con la variable MONGODB_URI (en producción): cada "archivo" se guarda
 *   como un documento en MongoDB Atlas, porque el hosting gratuito borra
 *   los archivos del disco cada vez que se reinicia.
 *
 * Los repositorios siguen trabajando igual: leen y escriben el JSON
 * completo, solo que ya no saben si va a disco o a Mongo.
 */
public final class Almacenamiento {

    private static final String MONGODB_URI = System.getenv("MONGODB_URI");

    private static MongoDatabase baseDatos;

    private Almacenamiento() {
    }

    private static boolean usaMongo() {
        return MONGODB_URI != null && !MONGODB_URI.isBlank();
    }

    private static synchronized MongoDatabase mongo() {

        if (baseDatos == null) {
            MongoClient cliente = MongoClients.create(MONGODB_URI);
            baseDatos = cliente.getDatabase("venta_condominio");
        }

        return baseDatos;
    }

    // =====================================================
    // Textos (clientes.json, usuarios.json, unidades.json)
    // =====================================================

    /** Devuelve el contenido guardado o null si todavía no existe. */
    public static String leerTexto(String nombre) {

        if (usaMongo()) {
            Document doc = mongo().getCollection("datos")
                    .find(Filters.eq("_id", nombre)).first();

            return doc == null ? null : doc.getString("contenido");
        }

        File archivo = archivo(nombre);

        if (!archivo.exists()) {
            return null;
        }

        try {
            return Files.readString(archivo.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer " + nombre, e);
        }
    }

    public static void escribirTexto(String nombre, String contenido) {

        if (usaMongo()) {
            mongo().getCollection("datos").replaceOne(
                    Filters.eq("_id", nombre),
                    new Document("_id", nombre).append("contenido", contenido),
                    new ReplaceOptions().upsert(true)
            );
            return;
        }

        try {
            Files.writeString(archivo(nombre).toPath(), contenido, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo escribir " + nombre, e);
        }
    }

    // =====================================================
    // Imágenes (uploads/perfiles, uploads/unidades)
    // =====================================================

    public static void guardarArchivo(String ruta, byte[] bytes) {

        if (usaMongo()) {
            MongoCollection<Document> archivos = mongo().getCollection("archivos");

            archivos.replaceOne(
                    Filters.eq("_id", ruta),
                    new Document("_id", ruta).append("bytes", new Binary(bytes)),
                    new ReplaceOptions().upsert(true)
            );
            return;
        }

        try {
            File destino = new File(directorioUploads(), ruta);
            destino.getParentFile().mkdirs();
            Files.write(destino.toPath(), bytes);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar la imagen", e);
        }
    }

    /** Devuelve los bytes de la imagen o null si no existe. */
    public static byte[] leerArchivo(String ruta) {

        if (usaMongo()) {
            Document doc = mongo().getCollection("archivos")
                    .find(Filters.eq("_id", ruta)).first();

            return doc == null ? null : doc.get("bytes", Binary.class).getData();
        }

        File origen = new File(directorioUploads(), ruta);

        try {
            // Evita que una ruta como "../usuarios.json" salga de uploads
            if (!origen.getCanonicalPath().startsWith(directorioUploads().getCanonicalPath())
                    || !origen.isFile()) {
                return null;
            }

            return Files.readAllBytes(origen.toPath());

        } catch (IOException e) {
            return null;
        }
    }

    // =====================================================
    // Carpetas en disco (solo modo local)
    // =====================================================

    private static File directorioBase() {

        String dataDir = System.getenv("DATA_DIR");

        File base = (dataDir == null || dataDir.isBlank())
                ? new File(".")
                : new File(dataDir);

        if (!base.exists()) {
            base.mkdirs();
        }

        return base;
    }

    private static File archivo(String nombre) {
        return new File(directorioBase(), nombre);
    }

    private static File directorioUploads() {
        return new File(directorioBase(), "uploads");
    }
}
