package venta_condominio.model;

/**
 * Estados de una reserva (F-04).
 *
 * PENDIENTE_PAGO la unidad quedó bloqueada; falta validar el pago de la cuota (F-13)
 * PAGADA         el pago de la cuota fue validado (F-13, por implementar)
 * CANCELADA      se anuló la reserva y la unidad volvió a estar disponible
 */
public enum EstadoReserva {
    PENDIENTE_PAGO,
    PAGADA,
    CANCELADA
}
