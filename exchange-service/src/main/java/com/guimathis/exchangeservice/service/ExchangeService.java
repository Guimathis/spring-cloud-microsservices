package com.guimathis.exchangeservice.service;

import com.guimathis.exchangeservice.environment.InstanceInformationService;
import com.guimathis.exchangeservice.exception.ExchangeNotSuportedException;
import com.guimathis.exchangeservice.model.Exchange;
import com.guimathis.exchangeservice.repository.ExchangeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ExchangeService {

    @Autowired
    ExchangeRepository exchangeRepository;
    @Autowired
    InstanceInformationService instanceInformationService;


    public Exchange doExchange(BigDecimal value, String from, String to) {
        Exchange exchange = exchangeRepository.findByFromAndTo(from, to)
                .orElseThrow(() -> new ExchangeNotSuportedException("Exchange not supported for " + from + " to " + to));

        exchange.setConvertedValue(getExchangeRate(value, exchange));
        exchange.setEnvironment("PORT " + instanceInformationService.retrieveServerPort());

        return exchange;
    }

    public BigDecimal getExchangeRate(BigDecimal value, Exchange exchange) {
        return exchange.getConversionFactor().multiply(value);
    }
}
