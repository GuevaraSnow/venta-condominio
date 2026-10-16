package venta_condominio.model;

/**
 * Estados de una cotización (F-03).
 *
 * GENERADA               lista para que el cliente la firme
 * PENDIENTE_AUTORIZACION descuento mayor al 2 %: espera al director (R003)
 * RECHAZADA              el director no aprobó el descuento
 * FIRMADA                el cliente aceptó las condiciones (requisito de R004)
 * VENCIDA                pasó su vigencia sin firmarse
 */
public enum EstadoCotizacion {
    GENERADA,
    PENDIENTE_AUTORIZACION,
    RECHAZADA,
    FIRMADA,
    VENCIDA
}
