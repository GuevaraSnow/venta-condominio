package venta_condominio.service;

import org.springframework.stereotype.Component;

import venta_condominio.patrones.EventoVenta;
import venta_condominio.patrones.ObservadorVenta;

/**
 * Observador concreto (patrón Observer): envía por correo cada evento de la venta
 * al interesado. El envío es asíncrono (@Async en ServicioEnvioEmail).
 */
@Component
public class NotificadorCorreo implements ObservadorVenta {

    private final ServicioEnvioEmail servicioEnvioEmail;

    public NotificadorCorreo(ServicioEnvioEmail servicioEnvioEmail) {
        this.servicioEnvioEmail = servicioEnvioEmail;
    }

    @Override
    public void alOcurrir(EventoVenta evento) {

        if (evento.destinatario() == null || evento.destinatario().isBlank()) {
            return;
        }

        servicioEnvioEmail.enviarNotificacion(evento.destinatario(), evento.asunto(), evento.mensaje());
    }
}
