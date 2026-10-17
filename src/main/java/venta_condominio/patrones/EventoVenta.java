package venta_condominio.patrones;

/**
 * Evento que se notifica a los observadores (patrón Observer).
 *
 * @param tipo         qué pasó, por ejemplo COTIZACION_GENERADA
 * @param destinatario correo de la persona a quien le interesa
 * @param asunto       asunto del aviso
 * @param mensaje      texto del aviso
 */
public record EventoVenta(String tipo, String destinatario, String asunto, String mensaje) {
}
