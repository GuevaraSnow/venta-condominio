package venta_condominio.patrones;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

import venta_condominio.model.Cliente;
import venta_condominio.model.Cotizacion;
import venta_condominio.model.EstadoCotizacion;
import venta_condominio.model.Unidad;
import venta_condominio.model.Usuario;

/**
 * PATRÓN FACTORY (creacional).
 *
 * Centraliza CÓMO se fabrica una cotización nueva: genera el id, aplica las
 * condiciones económicas del negocio y decide su estado inicial. El servicio
 * solo pide "créame una cotización" sin conocer esos detalles.
 *
 *  - cuota inicial = 30 % del precio final; el resto es saldo a financiar
 *  - vigencia de 15 días
 *  - R003: descuento mayor al 2 % nace PENDIENTE_AUTORIZACION
 */
public class CotizacionFactory {

    public static final BigDecimal PORCENTAJE_CUOTA_INICIAL = new BigDecimal("30");
    public static final BigDecimal DESCUENTO_SIN_AUTORIZACION = new BigDecimal("2");
    public static final int DIAS_VIGENCIA = 15;

    private static final BigDecimal CIEN = new BigDecimal("100");

    public record Calculo(BigDecimal valorDescuento, BigDecimal precioFinal,
                          BigDecimal cuotaInicial, BigDecimal saldoFinanciar) {
    }

    public static Calculo calcular(BigDecimal precioLista, BigDecimal porcentajeDescuento) {

        BigDecimal valorDescuento = precioLista.multiply(porcentajeDescuento)
                .divide(CIEN, 0, RoundingMode.HALF_UP);

        BigDecimal precioFinal = precioLista.subtract(valorDescuento);

        BigDecimal cuotaInicial = precioFinal.multiply(PORCENTAJE_CUOTA_INICIAL)
                .divide(CIEN, 0, RoundingMode.HALF_UP);

        return new Calculo(valorDescuento, precioFinal, cuotaInicial, precioFinal.subtract(cuotaInicial));
    }

    public static boolean requiereAutorizacion(BigDecimal porcentajeDescuento) {
        return porcentajeDescuento.compareTo(DESCUENTO_SIN_AUTORIZACION) > 0;
    }

    public Cotizacion crear(Unidad unidad, Cliente cliente, Usuario asesor,
                            BigDecimal porcentajeDescuento, LocalDate hoy) {

        Calculo calculo = calcular(unidad.getPrecioLista(), porcentajeDescuento);

        EstadoCotizacion estadoInicial = requiereAutorizacion(porcentajeDescuento)
                ? EstadoCotizacion.PENDIENTE_AUTORIZACION
                : EstadoCotizacion.GENERADA;

        String nombreCliente = (cliente.getNombre() + " " + cliente.getPrimerApellido() + " "
                + (cliente.getSegundoApellido() == null ? "" : cliente.getSegundoApellido())).trim();

        return new Cotizacion(
                UUID.randomUUID().toString(),
                unidad.getIdentificador(),
                cliente.getEmail(),
                nombreCliente,
                asesor.getEmail(),
                unidad.getPrecioLista(),
                porcentajeDescuento,
                calculo.valorDescuento(),
                calculo.precioFinal(),
                calculo.cuotaInicial(),
                calculo.saldoFinanciar(),
                hoy.toString(),
                hoy.plusDays(DIAS_VIGENCIA).toString(),
                estadoInicial,
                null,
                null
        );
    }
}
