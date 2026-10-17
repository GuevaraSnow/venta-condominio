package venta_condominio.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import venta_condominio.patrones.EventoVenta;
import venta_condominio.patrones.ObservadorVenta;

/**
 * Segundo observador concreto (patrón Observer): deja constancia en el log del
 * servidor de cada evento del proceso de venta (útil para auditoría en Render).
 */
@Component
public class BitacoraVentas implements ObservadorVenta {

    @Override
    public void alOcurrir(EventoVenta evento) {
        System.out.println("[VENTA] " + LocalDateTime.now() + " " + evento.tipo() + " -> " + evento.destinatario());
    }
}
