package venta_condominio.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@Service
public class VerificacionEmailService {

    private final Map<String, String> codigos = new HashMap<>();
    private final Set<String> correosVerificados = new HashSet<>();

    public String generarCodigo(String email) {

        String codigo = String.format(
                "%06d",
                new Random().nextInt(1_000_000)
        );

        codigos.put(email.toLowerCase(), codigo);

        return codigo;
    }

    public boolean verificarCodigo(String email, String codigo) {

        String emailNormalizado = email.toLowerCase();

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
                email.toLowerCase()
        );
    }
}