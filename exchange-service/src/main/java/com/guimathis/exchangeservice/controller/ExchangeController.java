package com.guimathis.exchangeservice.controller;

import com.guimathis.exchangeservice.model.Exchange;
import com.guimathis.exchangeservice.service.ExchangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Tag(name = "Exchange", description = "API for exchanging currencies")
@RestController
@RequestMapping("/exchange-service")
public class ExchangeController {

    @Autowired
    ExchangeService exchangeService;

    @Operation(summary = "Get exchange rate", description = "Get exchange rate for a given value and currency pair", parameters = {
            @Parameter(name = "value", description = "Value to be exchanged", required = true, example = "100.00"),
            @Parameter(name = "from", description = "Currency code to convert from", required = true, example = "USD"),
            @Parameter(name = "to", description = "Currency code to convert to", required = true, example = "BRL")
    })
    @GetMapping(value = "/{value}/{from}/{to}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Exchange getExchange(@PathVariable BigDecimal value, @PathVariable String from, @PathVariable String to) {
        return exchangeService.doExchange(value, from, to);
    }
}
