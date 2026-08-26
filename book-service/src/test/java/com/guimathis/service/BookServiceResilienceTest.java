package com.guimathis.service;

import com.guimathis.dto.ExchangeDTO;
import com.guimathis.environment.InstanceInformationService;
import com.guimathis.model.Book;
import com.guimathis.proxy.ExchangeClient;
import com.guimathis.repository.BookRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterAutoConfiguration;
import io.github.resilience4j.springboot3.retry.autoconfigure.RetryAutoConfiguration;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = {
                BookService.class,
                CircuitBreakerAutoConfiguration.class,
                RetryAutoConfiguration.class,
                RateLimiterAutoConfiguration.class,
                AopAutoConfiguration.class
        }, properties = {
        "resilience4j.retry.instances.exchange-service.max-attempts=3",
        "resilience4j.retry.instances.exchange-service.wait-duration=10ms",
        "resilience4j.circuitbreaker.instances.exchange-service.sliding-window-size=5",
        "resilience4j.circuitbreaker.instances.exchange-service.minimum-number-of-calls=5",
        "resilience4j.circuitbreaker.instances.exchange-service.failure-rate-threshold=50",
        "resilience4j.circuitbreaker.instances.exchange-service.wait-duration-in-open-state=5s",
        "resilience4j.ratelimiter.instances.exchange-service.limit-for-period=3",
        "resilience4j.ratelimiter.instances.exchange-service.limit-refresh-period=5s",
        "resilience4j.ratelimiter.instances.exchange-service.timeout-duration=0"
}
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookServiceResilienceTest {

    @MockitoBean
    private BookRepository bookRepository;

    @MockitoBean
    private ExchangeClient exchangeClient;

    @MockitoBean
    private InstanceInformationService informationService;

    @MockitoBean
    private ReviewService reviewService;

    private BookService bookService;
    private CircuitBreakerRegistry circuitBreakerRegistry;
    private RetryRegistry retryRegistry;
    private RateLimiterRegistry rateLimiterRegistry;

    @Autowired
    public BookServiceResilienceTest(BookService bookService,
                                     CircuitBreakerRegistry circuitBreakerRegistry,
                                     RetryRegistry retryRegistry,
                                     RateLimiterRegistry rateLimiterRegistry) {
        this.bookService = bookService;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    @BeforeEach
    void setUp() {
        circuitBreakerRegistry.circuitBreaker("exchange-service").reset();
    }

    @Test
    @DisplayName("Should retry when exchange client fails and succeed on recovery")
    void shouldRetryAndSucceedWhenExchangeRecovers() {
        UUID id = UUID.randomUUID();
        Book book = new Book(id, "Spring Cloud", "Author", "Publisher", 2024, new BigDecimal("100.00"), "USD");

        ExchangeDTO exchange = new ExchangeDTO();
        exchange.setConvertedValue(new BigDecimal("500.00"));
        exchange.setEnvironment("8000");

        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(informationService.retrieveHostName()).thenReturn("LOCAL");
        when(informationService.retrieveServerPort()).thenReturn("8100");
        when(exchangeClient.getExchange(new BigDecimal("100.00"), "USD", "BRL"))
                .thenThrow(new RuntimeException("Temporary network error"))
                .thenThrow(new RuntimeException("Temporary network error"))
                .thenReturn(exchange);
        final Retry transformerRetry = retryRegistry.retry("transformer");
        Optional<Book> result = transformerRetry.executeSupplier(() -> bookService.findByIdWithCurrency(id, "BRL"));

        assertThat(result).isPresent();
        Book foundBook = result.get();
        assertThat(foundBook.getPrice()).isEqualTo(new BigDecimal("500.00"));
        assertThat(foundBook.getCurrency()).isEqualTo("BRL");
        assertThat(foundBook.getEnvironment()).isEqualTo("Book-service HOST: LOCAL PORT: 8100 exchange-service HOST: 8000");

        verify(exchangeClient, times(3)).getExchange(new BigDecimal("100.00"), "USD", "BRL");
    }

    @Test
    @DisplayName("Should retry max attempts and execute fallback when exchange continues to fail")
    void shouldRetryMaxAttemptsAndExecuteFallbackWhenExchangeKeepsFailing() {
        UUID id = UUID.randomUUID();
        Book book = new Book(id, "Spring Cloud", "Author", "Publisher", 2024, new BigDecimal("100.00"), "USD");

        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(informationService.retrieveServerPort()).thenReturn("8100");
        when(exchangeClient.getExchange(new BigDecimal("100.00"), "USD", "BRL"))
                .thenThrow(new RuntimeException("Exchange service unavailable"));

        final Retry transformerRetry = retryRegistry.retry("transformer");
        Optional<Book> result = transformerRetry.executeSupplier(() -> bookService.findByIdWithCurrency(id, "BRL"));

        assertThat(result).isPresent();
        Book fallbackBook = result.get();
        assertThat(fallbackBook.getPrice()).isEqualTo(new BigDecimal("100.00"));
        assertThat(fallbackBook.getCurrency()).isEqualTo("USD");
        assertThat(fallbackBook.getEnvironment()).contains("Fallback ativado. Causa: Exchange service unavailable. Port: 8100");

        verify(exchangeClient, times(3)).getExchange(new BigDecimal("100.00"), "USD", "BRL");
    }

    @Test
    @DisplayName("Should return empty when book is not found in database during fallback")
    void shouldReturnEmptyWhenBookNotFoundInFallback() {
        UUID id = UUID.randomUUID();
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Book> result = bookService.findByIdWithCurrency(id, "BRL");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should open circuit breaker after failure threshold is reached and call fallback directly")
    void shouldOpenCircuitBreakerWhenFailureThresholdExceeded() {
        UUID id = UUID.randomUUID();
        Book book = new Book(id, "Spring Cloud", "Author", "Publisher", 2024, new BigDecimal("100.00"), "USD");

        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(informationService.retrieveServerPort()).thenReturn("8100");
        when(exchangeClient.getExchange(new BigDecimal("100.00"), "USD", "BRL"))
                .thenThrow(new RuntimeException("Exchange down"));

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("exchange-service");

        for (int i = 0; i < 5; i++) {
            bookService.findByIdWithCurrency(id, "BRL");
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        Optional<Book> resultWhenOpen = bookService.findByIdWithCurrency(id, "BRL");

        assertThat(resultWhenOpen).isPresent();
        assertThat(resultWhenOpen.get().getEnvironment()).contains("Fallback ativado");
    }

    @Test
    @DisplayName("Should trigger rate limiter and fallback when request limit is exceeded")
    void shouldTriggerRateLimiterWhenLimitExceeded() {

        ExchangeDTO exchange = new ExchangeDTO();
        exchange.setConvertedValue(new BigDecimal("500.00"));
        exchange.setEnvironment("8000");

        when(bookRepository.findById(any())).thenAnswer(inv ->
                Optional.of(new Book(UUID.randomUUID(), "Titulo", "Autor", "Editora", 2024, new BigDecimal("100.00"), "USD"))
        );
        when(informationService.retrieveHostName()).thenReturn("LOCAL");
        when(informationService.retrieveServerPort()).thenReturn("8100");
        when(exchangeClient.getExchange(new BigDecimal("100.00"), "USD", "BRL")).thenReturn(exchange);

        Optional<Book> call1 = bookService.findByIdWithCurrency(any(), "BRL");
        Optional<Book> call2 = bookService.findByIdWithCurrency(any(), "BRL");
          bookService.findByIdWithCurrency(any(), "BRL");
        Optional<Book> call4 = bookService.findByIdWithCurrency(any(), "BRL");

        assertThat(call1).isPresent();
        assertThat(call1.get().getEnvironment()).contains("exchange-service HOST: 8000");

        assertThat(call2).isPresent();
        assertThat(call2.get().getEnvironment()).contains("exchange-service HOST: 8000");

        assertThat(call4).isPresent();
        assertThat(call4.get().getEnvironment()).contains("Fallback ativado");
        assertThat(call4.get().getEnvironment()).contains("RateLimiter 'exchange-service' does not permit further calls");
    }
}
