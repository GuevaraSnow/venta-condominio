package venta_condominio.patrones;

/**
 * PATRÓN OBSERVER (comportamiento) — el "observador".
 * Cualquier clase que quiera enterarse de lo que pasa en el proceso de venta
 * implementa esta interfaz (correo al cliente, bitácora, etc.).
 */
public interface ObservadorVenta {

    void alOcurrir(EventoVenta evento);
}
