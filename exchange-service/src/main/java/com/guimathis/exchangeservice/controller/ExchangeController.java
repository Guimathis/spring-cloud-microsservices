package com.guimathis.exchangeservice.controller;

import com.guimathis.exchangeservice.model.Exchange;
import com.guimathis.exchangeservice.service.ExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/exchange-service")
public class ExchangeController {

    @Autowired
    ExchangeService exchangeService;

    @GetMapping(value = "/{value}/{from}/{to}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Exchange getExchange(@PathVariable BigDecimal value, @PathVariable String from, @PathVariable String to) {
        return exchangeService.doExchange(value, from, to);
    }
}
