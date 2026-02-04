package com.cinebook.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CineBook API")
                        .version("1.0.0")
                        .description("API REST para sistema de reserva de entradas de cine")
                        .contact(new Contact()
                                .name("Nicolás Baez")
                                .email("nicolasbaez1201@gmail.com")
                                .url("https://github.com/baez-nicolas/cinebook-app")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor Local de Desarrollo")
                ));
    }
}