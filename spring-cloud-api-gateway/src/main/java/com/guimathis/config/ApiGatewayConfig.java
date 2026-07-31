// Configurações movidas para o properties, para que o openApi possa coletar dinamicamente as rotas utilizadas pelos serviços
// e possa redirecionar o Swagger UI para cada url correspondente a partir do OpenApiConfiguration.java

/*
package com.guimathis.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiGatewayConfig {

    @Bean
    public RouteLocator gatewayRouter(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(p -> p.path("/book/**")
                        .uri("lb://book-service"))
                .route(p -> p.path("/exchange-service/**")
                        .uri("lb://exchange-service"))
                .build();
    }
}
*/
