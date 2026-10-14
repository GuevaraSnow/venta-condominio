package venta_condominio.config;

import venta_condominio.util.Almacenamiento;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Orígenes (URLs del frontend) autorizados a llamar al backend.
     * En local es http://localhost:5173; en producción se define la
     * variable CORS_ORIGINS con la URL de Vercel (separadas por coma
     * si hay varias).
     */
    @Value("${app.cors.origins}")
    private String[] origenesPermitidos;

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/**")
                .allowedOriginPatterns(origenesPermitidos)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String ubicacion = Almacenamiento.directorioUploads()
                .toPath().toAbsolutePath().normalize().toUri().toString();

        if (!ubicacion.endsWith("/")) {
            ubicacion += "/";
        }

        registry.addResourceHandler("/archivos/**")
                .addResourceLocations(ubicacion);
    }
}