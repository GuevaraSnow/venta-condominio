package venta_condominio.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class VerificacionEmailService {

    private final Map<String, String> codigos = new HashMap<>();
    private final Set<String> correosVerificados = new HashSet<>();

    private final ServicioEnvioEmail servicioEnvioEmail;

    public VerificacionEmailService(
            ServicioEnvioEmail servicioEnvioEmail) {

        this.servicioEnvioEmail = servicioEnvioEmail;
    }

    public void generarCodigo(String email) {

        String emailNormalizado = email.trim().toLowerCase();

        String codigo = String.format(
                "%06d",
                new SecureRandom().nextInt(1_000_000)
        );

        codigos.put(emailNormalizado, codigo);

        servicioEnvioEmail.enviarNotificacion(
                emailNormalizado,
                "Código de verificación - Venta Condominio",
                "Hola.\n\n"
                        + "Tu código de verificación para Venta Condominio es:\n\n"
                        + codigo
                        + "\n\n"
                        + "Este código es necesario para completar el registro."
        );
    }

    public boolean verificarCodigo(String email, String codigo) {

        String emailNormalizado = email.trim().toLowerCase();

        String codigoGuardado = codigos.get(emailNormalizado);

        if (codigoGuardado == null) {
            return false;
        }

        if (!codigoGuardado.equals(codigo)) {
            return false;
        }

        codigos.remove(emailNormalizado);
        correosVerificados.add(emailNormalizado);

        return true;
    }

    public boolean estaVerificado(String email) {

        return correosVerificados.contains(
                email.trim().toLowerCase()
        );
    }
    void marcarComoVerificado(String email) {

        correosVerificados.add(
                email.trim().toLowerCase()
        );
    }
}