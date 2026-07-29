package com.guimathis.service;

import com.guimathis.dto.ExchangeDTO;
import com.guimathis.environment.InstanceInformationService;
import com.guimathis.model.Book;
import com.guimathis.proxy.ExchangeClient;
import com.guimathis.repository.BookRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookService {

    private final Logger logger = LoggerFactory.getLogger(BookService.class);

    @Autowired
    private InstanceInformationService informationService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ExchangeClient exchangeClient;

    // Aplicando Circuit Breaker e Retry na chamada ao exchange-service
    @Retry(name = "exchange-service")
    @CircuitBreaker(name = "exchange-service", fallbackMethod = "getExchangeFallback")
    @RateLimiter(name = "exchange-service")
    public Book findBook(Long id, String currency) {

        var book = bookRepository.findById(id).orElseThrow();

        // Operação que pode falhar se o exchange-service estiver fora do ar
        ExchangeDTO exchange = exchangeClient.getExchange(book.getPrice(), "USD", currency);

        book.setPrice(exchange.getConvertedValue());
        book.setCurrency(currency);

        book.setEnvironment("Book-service PORT: " + informationService.retrieveServerPort() + " exchange-service PORT: " + exchange.getEnvironment());
        logger.info("Requisicao Processada: Book-service PORT: {} exchange-service PORT: {}", informationService.retrieveServerPort(), exchange.getEnvironment());
        return book;
    }

    // Métödo de Fallback que será chamado se o Circuit Breaker abrir ou a chamada falhar
    private Book getExchangeFallback(Long id, String currency, Exception e) {
        logger.warn("Exchange Service Indisponivel: {}", e.getMessage());
        var book = bookRepository.findById(id).orElseThrow();
        book.setCurrency(currency);
        book.setEnvironment("Fallback: Exchange Service Indisponível. Port: " + informationService.retrieveServerPort());
        // Retorna um valor padrão ou logica alternativa para não quebrar o fluxo
        book.setPrice(book.getPrice());
        return book;
    }
/*     Antes do feignClient
    public Book findBook(Long id, String currency) {
        String port = informationService.retrieveServerPort();

        var book = repository.findById(id).orElseThrow();

        HashMap<String, String> params = new HashMap<>();
        params.put("amount", book.getPrice().toString());
        params.put("from", "USD");
        params.put("to", currency);

        var response = new RestTemplate()
                .getForEntity(API_URL, ExchangeDTO.class, params);

        ExchangeDTO exchange = response.getBody();
        if (exchange == null) {
            throw new RuntimeException("Failed to retrieve exchange rate");
        }
        book.setEnvironment(port);
        book.setPrice(exchange.getConvertedValue());
        book.setCurrency(currency);
        return book;
    }*/
}
