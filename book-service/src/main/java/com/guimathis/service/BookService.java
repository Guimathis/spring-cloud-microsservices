package com.guimathis.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.guimathis.dto.ExchangeDTO;
import com.guimathis.dto.BookIndexResponse;
import com.guimathis.dto.BookRecordDto;
import com.guimathis.environment.InstanceInformationService;
import com.guimathis.model.Book;
import com.guimathis.proxy.ExchangeClient;
import com.guimathis.repository.BookRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class BookService {

    private final Logger logger = LoggerFactory.getLogger(BookService.class);

    private final InstanceInformationService informationService;
    private final BookRepository bookRepository;
    private final ExchangeClient exchangeClient;
    private final ReviewService reviewService;

    public BookService(InstanceInformationService informationService,
                       BookRepository bookRepository,
                       ExchangeClient exchangeClient,
                       ReviewService reviewService) {
        this.informationService = informationService;
        this.bookRepository = bookRepository;
        this.exchangeClient = exchangeClient;
        this.reviewService = reviewService;
    }

    public List<BookIndexResponse> findAll() {
        return bookRepository.findAll().stream()
                .map(book -> new BookIndexResponse(
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getPublisher(),
                        book.getPublicationYear(),
                        book.getPrice(),
                        book.getReview(),
                        book.getCurrency()
                ))
                .collect(Collectors.toList());
    }

    public Optional<Book> findById(UUID id) {
        return bookRepository.findById(id);
    }

    @Retry(name = "exchange-service", fallbackMethod = "getExchangeFallback"  )
    @CircuitBreaker(name = "exchange-service")
    @RateLimiter(name = "exchange-service")
    public Optional<Book> findByIdWithCurrency(UUID id, String currency) {
        var bookOptional = bookRepository.findById(id);
        if (bookOptional.isEmpty()) {
            return Optional.empty();
        }

        var book = bookOptional.get();

        ExchangeDTO exchange = exchangeClient.getExchange(book.getPrice(), "USD", currency);

        book.setPrice(exchange.getConvertedValue());
        book.setCurrency(currency);

        book.setEnvironment("Book-service HOST: " + informationService.retrieveHostName()
                + " PORT: " + informationService.retrieveServerPort()
                + " exchange-service HOST: " + exchange.getEnvironment());

        return Optional.of(book);
    }

    public Book save(BookRecordDto bookRecordDto) {
        var book = new Book();
        BeanUtils.copyProperties(bookRecordDto, book);
        book.setReview(reviewService.generateReview(book.getTitle()));
        return bookRepository.save(book);
    }

    public Book update(Book book, BookRecordDto bookRecordDto) {
        BeanUtils.copyProperties(bookRecordDto, book);
        return bookRepository.save(book);
    }

    public void delete(Book book) {
        bookRepository.delete(book);
    }

    private Optional<Book> getExchangeFallback(UUID id, String currency, Throwable e) {
        logger.warn("Fallback ativado para o livro {}. Causa: {}", id, e.getMessage());
        var bookOptional = bookRepository.findById(id);
        if (bookOptional.isEmpty()) {
            return Optional.empty();
        }

        var book = bookOptional.get();
        book.setCurrency(book.getCurrency());
        book.setEnvironment("Fallback ativado. Causa: " + e.getMessage() + ". Port: " + informationService.retrieveServerPort());
        book.setPrice(book.getPrice());
        return Optional.of(book);
    }
}
