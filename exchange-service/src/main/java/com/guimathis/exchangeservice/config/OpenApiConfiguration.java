package com.guimathis.exchangeservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;

@OpenAPIDefinition(info = @Info(title = "Exchange Service API", description = "Api for consulting Exchange rates", version = "1.0", license = @License(name = "Apache 2.0", url = "")))
public class OpenApiConfiguration {
}