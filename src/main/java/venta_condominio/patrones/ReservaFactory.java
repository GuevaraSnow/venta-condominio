package venta_condominio.patrones;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

import venta_condominio.model.Cotizacion;
import venta_condominio.model.EstadoReserva;
import venta_condominio.model.Reserva;
import venta_condominio.model.Unidad;

/**
 * PATRÓN FACTORY (creacional).
 *
 * Fabrica la reserva a partir de la cotización firmada y calcula la cuota
 * con la regla R002: 1 % del precio de lista, mínimo $5.000.000.
 */
public class ReservaFactory {

    public static final BigDecimal PORCENTAJE_CUOTA_RESERVA = new BigDecimal("1");
    public static final BigDecimal CUOTA_MINIMA = new BigDecimal("5000000");

    public static BigDecimal calcularCuota(BigDecimal precioLista) {

        BigDecimal unoPorCiento = precioLista.multiply(PORCENTAJE_CUOTA_RESERVA)
                .divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);

        return unoPorCiento.max(CUOTA_MINIMA);
    }

    public Reserva crear(Cotizacion cotizacion, Unidad unidad, String creadaPor, LocalDate hoy) {
        return new Reserva(
                UUID.randomUUID().toString(),
                cotizacion.getId(),
                unidad.getIdentificador(),
                cotizacion.getEmailCliente(),
                cotizacion.getNombreCliente(),
                creadaPor,
                unidad.getPrecioLista(),
                cotizacion.getPrecioFinal(),
                calcularCuota(unidad.getPrecioLista()),
                hoy.toString(),
                EstadoReserva.PENDIENTE_PAGO,
                null
        );
    }
}
