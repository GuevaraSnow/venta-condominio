package venta_condominio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import venta_condominio.model.Cliente;
import venta_condominio.patrones.CotizacionFactory;

/**
 * Precalificación del cliente (adelanto de F-05 / R005).
 *
 * Con los datos que el cliente DECLARÓ al registrarse se revisa si una unidad
 * está a su alcance antes de cotizarla:
 *
 *  1. Cuota inicial: el 30 % del precio final debe caber en el presupuesto
 *     disponible para la cuota inicial.
 *  2. Capacidad de pago (R005): el 70 % restante se simula como crédito
 *     hipotecario de cuota fija y esa cuota no puede superar el 30 % de los
 *     ingresos mensuales del hogar (el campo "salario" suma los ingresos de
 *     quienes compran juntos: pareja o codeudor, como lo aceptan los bancos).
 *
 * Es una PRECALIFICACIÓN: la validación definitiva la hace la entidad
 * financiera en F-05. La tasa es una referencia para el proyecto.
 */
public final class Precalificacion {

    /** Tasa de referencia del crédito hipotecario: 12 % efectivo anual. */
    public static final double TASA_EFECTIVA_ANUAL = 0.12;

    /** Plazo del crédito: 20 años. */
    public static final int PLAZO_MESES = 240;

    /** R005: la cuota mensual no puede pasar del 30 % del ingreso. */
    public static final BigDecimal PORCENTAJE_MAXIMO_INGRESO = new BigDecimal("30");

    private static final BigDecimal CIEN = new BigDecimal("100");

    private Precalificacion() {
    }

    public record Resultado(
            BigDecimal precioFinal,
            BigDecimal cuotaInicial,
            BigDecimal saldoFinanciar,
            BigDecimal cuotaMensual,
            BigDecimal salarioMinimo,
            BigDecimal presupuesto,
            BigDecimal salario,
            boolean alcanzaCuotaInicial,
            boolean alcanzaCuotaMensual) {

        public boolean aprobado() {
            return alcanzaCuotaInicial && alcanzaCuotaMensual;
        }

        /** Explica por qué no pasa (null si pasa). */
        public String motivo() {
            if (!alcanzaCuotaInicial) {
                return "La cuota inicial (" + CotizacionService.pesos(cuotaInicial)
                        + ") supera el presupuesto disponible (" + CotizacionService.pesos(presupuesto) + ")";
            }
            if (!alcanzaCuotaMensual) {
                return "La cuota mensual estimada (" + CotizacionService.pesos(cuotaMensual)
                        + ") supera el 30 % de los ingresos del hogar; se requieren ingresos de al menos "
                        + CotizacionService.pesos(salarioMinimo);
            }
            return null;
        }
    }

    /** Cuota fija mensual: monto × i / (1 − (1 + i)^−n). */
    public static BigDecimal cuotaMensual(BigDecimal montoFinanciar) {

        if (montoFinanciar == null || montoFinanciar.signum() <= 0) {
            return BigDecimal.ZERO;
        }

        double i = Math.pow(1 + TASA_EFECTIVA_ANUAL, 1.0 / 12) - 1;
        double cuota = montoFinanciar.doubleValue() * i / (1 - Math.pow(1 + i, -PLAZO_MESES));

        return BigDecimal.valueOf(cuota).setScale(0, RoundingMode.HALF_UP);
    }

    /** Salario necesario para que la cuota sea el 30 % del ingreso. */
    public static BigDecimal salarioMinimo(BigDecimal cuotaMensual) {
        return cuotaMensual.multiply(CIEN)
                .divide(PORCENTAJE_MAXIMO_INGRESO, 0, RoundingMode.CEILING);
    }

    public static Resultado evaluar(Cliente cliente, BigDecimal precioLista, BigDecimal porcentajeDescuento) {

        CotizacionFactory.Calculo calculo = CotizacionFactory.calcular(precioLista,
                porcentajeDescuento == null ? BigDecimal.ZERO : porcentajeDescuento);

        BigDecimal cuota = cuotaMensual(calculo.saldoFinanciar());
        BigDecimal minimo = salarioMinimo(cuota);

        BigDecimal presupuesto = cliente.getPresupuesto() == null ? BigDecimal.ZERO : cliente.getPresupuesto();
        BigDecimal salario = cliente.getSalario() == null ? BigDecimal.ZERO : cliente.getSalario();

        return new Resultado(
                calculo.precioFinal(),
                calculo.cuotaInicial(),
                calculo.saldoFinanciar(),
                cuota,
                minimo,
                presupuesto,
                salario,
                calculo.cuotaInicial().compareTo(presupuesto) <= 0,
                salario.compareTo(minimo) >= 0
        );
    }
}
