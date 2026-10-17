package venta_condominio.model;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Roles del sistema (simplificados a 3).
 *
 * CLIENTE            se registra, consulta unidades y firma sus cotizaciones
 * ASESOR_COMERCIAL   atiende al cliente, genera cotizaciones y reservas
 * DIRECTOR_COMERCIAL todo lo del asesor + crea/edita unidades y autoriza descuentos (R003)
 *
 * Los roles antiguos ABOGADO y ADMINISTRADOR_CONJUNTO se eliminaron; si aparecen
 * en datos guardados se convierten al rol equivalente para no romper la lectura.
 */
public enum Rol {

    CLIENTE,
    ASESOR_COMERCIAL,
    DIRECTOR_COMERCIAL;

    @JsonCreator
    public static Rol desdeTexto(String valor) {

        if (valor == null) {
            return null;
        }

        return switch (valor.trim().toUpperCase()) {
            case "ADMINISTRADOR_CONJUNTO" -> DIRECTOR_COMERCIAL;
            case "ABOGADO" -> ASESOR_COMERCIAL;
            default -> Rol.valueOf(valor.trim().toUpperCase());
        };
    }
}
