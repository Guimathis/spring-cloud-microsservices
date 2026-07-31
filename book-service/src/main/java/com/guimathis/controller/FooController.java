package com.guimathis.controller;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Tag(name = "Foo endpoint", description = "Operations related to foo")
@RestController
@RequestMapping("/foo")
public class FooController {

    private static final String BACKEND = "backend";

    @CircuitBreaker(name = BACKEND, fallbackMethod = "fallback")
    @RateLimiter(name = BACKEND)
    @Bulkhead(name = BACKEND, fallbackMethod = "fallback")
    @Retry(name = BACKEND)
    @TimeLimiter(name = BACKEND)
    @GetMapping("/foo")
    public Mono<String> fooMethod(String param1) {
        return Mono.error(new NumberFormatException());
    }

    private Mono<String> fallback(String param1, CallNotPermittedException e) {
        return Mono.just("Handled the exception when the CircuitBreaker is open");
    }

    private Mono<String> fallback(String param1, BulkheadFullException e) {
        return Mono.just("Handled the exception when the Bulkhead is full");
    }

    private Mono<String> fallback(String param1, NumberFormatException e) {
        return Mono.just("Handled the NumberFormatException");
    }

    private Mono<String> fallback(String param1, Exception e) {
        return Mono.just("Handled any other exception");
    }
}
