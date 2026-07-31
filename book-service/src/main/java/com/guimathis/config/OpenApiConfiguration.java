package com.guimathis.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@OpenAPIDefinition(info = @Info(title = "Book Service API", description = "Api for consulting books", version = "1.0", license = @License(name = "Apache 2.0", url = "")))
public class OpenApiConfiguration {
}