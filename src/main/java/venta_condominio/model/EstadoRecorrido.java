package venta_condominio.model;

/**
 * Estados de un recorrido por el conjunto (F-02).
 *
 * AGENDADO   la visita está programada; la unidad sigue DISPONIBLE
 * EN_CURSO   el asesor inició el recorrido; la unidad pasa a EN_RECORRIDO
 * FINALIZADO terminó la visita; la unidad vuelve a DISPONIBLE
 * CANCELADO  se anuló antes de iniciar
 */
public enum EstadoRecorrido {
    AGENDADO,
    EN_CURSO,
    FINALIZADO,
    CANCELADO
}
