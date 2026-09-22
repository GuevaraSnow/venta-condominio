package venta_condominio.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class ServicioEnvioEmail {

    private final JavaMailSender mailSender;

    public ServicioEnvioEmail(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarNotificacion(
            String destinatario,
            String asunto,
            String mensaje) {

        SimpleMailMessage correo = new SimpleMailMessage();

        correo.setFrom("fuentesthomasito777@gmail.com");
        correo.setTo(destinatario);
        correo.setSubject(asunto);
        correo.setText(mensaje);

        mailSender.send(correo);
    }
}