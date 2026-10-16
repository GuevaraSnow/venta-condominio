package venta_condominio.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import venta_condominio.exception.UsuarioException;
import venta_condominio.model.Usuario;
import venta_condominio.repository.UsuarioRepositorio;

/**
 * AC-05: recuperar la contraseña con un código de 6 dígitos enviado al correo.
 *
 * - El código vence a los 10 minutos.
 * - Se puede usar una sola vez.
 * - Después de 5 intentos fallidos se invalida y hay que pedir otro.
 */
@Service
public class RecuperacionContrasenaService {

    private static final int MINUTOS_VIGENCIA = 10;
    private static final int MAX_INTENTOS = 5;
    private static final int LONGITUD_MINIMA_CONTRASENA = 6;

    private final UsuarioRepositorio usuarioRepositorio;
    private final ServicioEnvioEmail servicioEnvioEmail;
    private final SecureRandom aleatorio = new SecureRandom();

    private final Map<String, CodigoRecuperacion> codigos = new ConcurrentHashMap<>();

    public RecuperacionContrasenaService(
            UsuarioRepositorio usuarioRepositorio,
            ServicioEnvioEmail servicioEnvioEmail) {

        this.usuarioRepositorio = usuarioRepositorio;
        this.servicioEnvioEmail = servicioEnvioEmail;
    }

    /**
     * Envía el código solo si el correo existe, pero no le dice a quien
     * pregunta si existe o no (así nadie puede averiguar qué correos
     * están registrados).
     */
    public void solicitarCodigo(String email) {

        if (email == null || email.isBlank()) {
            throw new UsuarioException("El correo es obligatorio");
        }

        String emailNormalizado = email.trim().toLowerCase();

        Usuario usuario = usuarioRepositorio.buscarPorEmail(emailNormalizado);

        if (usuario == null) {
            return;
        }

        String codigo = String.format("%06d", aleatorio.nextInt(1_000_000));

        codigos.put(emailNormalizado, new CodigoRecuperacion(
                codigo,
                LocalDateTime.now().plusMinutes(MINUTOS_VIGENCIA)
        ));

        servicioEnvioEmail.enviarNotificacion(
                emailNormalizado,
                "Recuperar contraseña - Venta Condominio",
                "Hola " + usuario.getNombre() + ".\n\n"
                        + "Tu código para restablecer la contraseña es:\n\n"
                        + codigo
                        + "\n\nVence en " + MINUTOS_VIGENCIA + " minutos.\n\n"
                        + "Si no pediste este cambio, ignora este correo: "
                        + "tu contraseña actual sigue funcionando."
        );
    }

    public void restablecerContrasena(String email, String codigo, String nuevaContrasena) {

        if (email == null || codigo == null || nuevaContrasena == null) {
            throw new UsuarioException("Correo, código y nueva contraseña son obligatorios");
        }

        if (nuevaContrasena.length() < LONGITUD_MINIMA_CONTRASENA) {
            throw new UsuarioException(
                    "La contraseña debe tener al menos " + LONGITUD_MINIMA_CONTRASENA + " caracteres");
        }

        String emailNormalizado = email.trim().toLowerCase();

        CodigoRecuperacion guardado = codigos.get(emailNormalizado);

        if (guardado == null) {
            throw new UsuarioException("El código no es válido. Solicita uno nuevo.");
        }

        if (LocalDateTime.now().isAfter(guardado.vence())) {
            codigos.remove(emailNormalizado);
            throw new UsuarioException("El código venció. Solicita uno nuevo.");
        }

        if (!guardado.codigo().equals(codigo.trim())) {

            int intentos = guardado.intentos() + 1;

            if (intentos >= MAX_INTENTOS) {
                codigos.remove(emailNormalizado);
                throw new UsuarioException("Demasiados intentos. Solicita un código nuevo.");
            }

            codigos.put(emailNormalizado, guardado.conIntento(intentos));
            throw new UsuarioException("El código es incorrecto");
        }

        Usuario usuario = usuarioRepositorio.buscarPorEmail(emailNormalizado);

        if (usuario == null) {
            codigos.remove(emailNormalizado);
            throw new UsuarioException("El código no es válido. Solicita uno nuevo.");
        }

        usuarioRepositorio.actualizar(usuario.conContrasena(nuevaContrasena));

        // Un solo uso
        codigos.remove(emailNormalizado);
    }

    private record CodigoRecuperacion(String codigo, LocalDateTime vence, int intentos) {

        CodigoRecuperacion(String codigo, LocalDateTime vence) {
            this(codigo, vence, 0);
        }

        CodigoRecuperacion conIntento(int nuevosIntentos) {
            return new CodigoRecuperacion(codigo, vence, nuevosIntentos);
        }
    }
}
