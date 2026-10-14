package venta_condominio.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Habilita el uso de @Async en el proyecto. Sin esta clase, la anotación
 * @Async se ignora silenciosamente y los métodos siguen corriendo de
 * forma síncrona (bloqueante).
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}