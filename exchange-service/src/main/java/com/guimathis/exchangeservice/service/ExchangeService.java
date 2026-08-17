package com.guimathis.exchangeservice.service;

import com.guimathis.exchangeservice.environment.InstanceInformationService;
import com.guimathis.exchangeservice.exception.ExchangeNotSuportedException;
import com.guimathis.exchangeservice.model.Exchange;
import com.guimathis.exchangeservice.repository.ExchangeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ExchangeService {

    private Logger logger = LoggerFactory.getLogger(ExchangeService.class);

    @Autowired
    ExchangeRepository exchangeRepository;

    @Autowired
    InstanceInformationService instanceInformationService;


    public Exchange doExchange(BigDecimal value, String from, String to) {
        Exchange exchange = exchangeRepository.findByFromAndTo(from, to)
                .orElseThrow(() -> new ExchangeNotSuportedException("Exchange not supported for " + from + " to " + to));

        logger.info("Calculando exchange rate: {} x {} = {} {}", value, from, getExchangeRate(value, exchange), to);

        exchange.setConvertedValue(getExchangeRate(value, exchange));
        exchange.setEnvironment(instanceInformationService.retrieveHostName() +  " VERSION KUBE-V2 PORT: " + instanceInformationService.retrieveServerPort());

        return exchange;
    }

    public BigDecimal getExchangeRate(BigDecimal value, Exchange exchange) {
        return exchange.getConversionFactor().multiply(value);
    }
}
