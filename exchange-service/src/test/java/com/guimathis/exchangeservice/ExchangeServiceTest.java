package com.guimathis.exchangeservice;

import com.guimathis.exchangeservice.exception.ExchangeNotSuportedException;
import com.guimathis.exchangeservice.repository.ExchangeRepository;
import com.guimathis.exchangeservice.service.ExchangeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExchangeServiceTest {

    @Mock
    private ExchangeRepository repository;

    @InjectMocks
    private ExchangeService service;

    @Test
    void shouldThrowExceptionWhenExchangeNotFound() {
        when(repository.findByFromAndTo("USD", "USD")).thenReturn(Optional.empty());

        ExchangeNotSuportedException exception = assertThrows(ExchangeNotSuportedException.class, () -> {
            service.doExchange(BigDecimal.TEN, "USD", "USD");
        });

        assertEquals("Exchange not supported for USD to USD", exception.getMessage());
    }
}
