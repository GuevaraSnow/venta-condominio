package venta_condominio.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Envía correos de dos formas:
 *  - Si existe la variable BREVO_API_KEY, usa la API HTTP de Brevo
 *    (funciona en hostings que bloquean SMTP, como en producción).
 *  - Si no, usa SMTP de Gmail con MAIL_USERNAME / MAIL_PASSWORD (local).
 */
@Service
public class ServicioEnvioEmail {

    private static final String URL_BREVO = "https://api.brevo.com/v3/smtp/email";

    private final ObjectProvider<JavaMailSender> mailSender;
    private final RestClient restClient = RestClient.create();

    @Value("${app.mail.brevo-api-key:}")
    private String brevoApiKey;

    @Value("${app.mail.remitente:}")
    private String remitente;

    public ServicioEnvioEmail(ObjectProvider<JavaMailSender> mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void enviarNotificacion(
            String destinatario,
            String asunto,
            String mensaje) {

        try {
            if (brevoApiKey != null && !brevoApiKey.isBlank()) {
                enviarConBrevo(destinatario, asunto, mensaje);
            } else {
                enviarConSmtp(destinatario, asunto, mensaje);
            }

        } catch (Exception e) {
            // Al ser @Async, si no se registra aquí el error se pierde.
            System.err.println("No se pudo enviar el correo a " + destinatario
                    + ": " + e.getMessage());
        }
    }

    private void enviarConBrevo(String destinatario, String asunto, String mensaje) {

        Map<String, Object> cuerpo = Map.of(
                "sender", Map.of("name", "Venta Condominio", "email", remitente),
                "to", List.of(Map.of("email", destinatario)),
                "subject", asunto,
                "textContent", mensaje
        );

        restClient.post()
                .uri(URL_BREVO)
                .header("api-key", brevoApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(cuerpo)
                .retrieve()
                .toBodilessEntity();
    }

    private void enviarConSmtp(String destinatario, String asunto, String mensaje) {

        JavaMailSender sender = mailSender.getIfAvailable();

        if (sender == null) {
            throw new IllegalStateException("No hay configuración de correo (BREVO_API_KEY o MAIL_USERNAME)");
        }

        SimpleMailMessage correo = new SimpleMailMessage();

        correo.setFrom(remitente);
        correo.setTo(destinatario);
        correo.setSubject(asunto);
        correo.setText(mensaje);

        sender.send(correo);
    }
}
