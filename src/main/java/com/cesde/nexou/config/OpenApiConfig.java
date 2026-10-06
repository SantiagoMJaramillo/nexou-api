package com.cesde.nexou.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.scalar.maven.webmvc.ScalarWebMvcAutoConfiguration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

/**
 * Información general de la API que se muestra en Swagger UI (/swagger-ui.html)
 * y en Scalar (/scalar).
 * Se importa la configuración de Scalar de forma explícita porque Spring Boot 4
 * no la detecta automáticamente y /scalar respondía 404.
 */
@Configuration
@Import(ScalarWebMvcAutoConfiguration.class)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NEXOU API - Reservas de biblioteca y equipos tecnológicos")
                        .version("1.0.0")
                        .description("API REST para gestionar usuarios, su configuración, categorías, libros, "
                                + "equipos tecnológicos y las reservas (préstamo, renovación y devolución) de libros "
                                + "y equipos. Incluye manejo global de excepciones y siembra de datos de prueba.")
                        .contact(new Contact()
                                .name("Equipo NEXOU - CESDE")
                                .url("https://github.com/SantiagoMJaramillo/nexou-api"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
