package com.guimathis.proxy;

import com.guimathis.dto.ExchangeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "exchange-service", url = "${EXCHANGE_SERVICE_URL:http://localhost:8000}")
public interface ExchangeClient {

    @GetMapping(value = "exchange-service/{value}/{from}/{to}", produces = MediaType.APPLICATION_JSON_VALUE)
    ExchangeDTO getExchange(@PathVariable Double value, @PathVariable String from, @PathVariable String to);
}
