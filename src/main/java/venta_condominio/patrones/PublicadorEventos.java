package venta_condominio.patrones;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * PATRÓN OBSERVER (comportamiento) — el "sujeto".
 *
 * Los servicios del proceso de venta (cotizaciones, reservas, recorridos)
 * extienden esta clase. Cuando ocurre algo importante llaman a notificar(...)
 * y todos los observadores suscritos reaccionan, sin que el servicio sepa
 * quiénes son ni qué hacen (bajo acoplamiento).
 *
 * Spring inyecta automáticamente todos los @Component que implementan
 * ObservadorVenta; en las pruebas se suscriben a mano con suscribir(...).
 */
public abstract class PublicadorEventos {

    private final List<ObservadorVenta> observadores = new ArrayList<>();

    @Autowired(required = false)
    public void setObservadores(List<ObservadorVenta> observadoresSpring) {
        observadores.clear();
        observadores.addAll(observadoresSpring);
    }

    public void suscribir(ObservadorVenta observador) {
        observadores.add(observador);
    }

    protected void notificar(EventoVenta evento) {

        for (ObservadorVenta observador : observadores) {
            try {
                observador.alOcurrir(evento);
            } catch (Exception e) {
                // Un observador que falla no debe tumbar la operación principal
                System.err.println("Observador " + observador.getClass().getSimpleName()
                        + " falló: " + e.getMessage());
            }
        }
    }
}
