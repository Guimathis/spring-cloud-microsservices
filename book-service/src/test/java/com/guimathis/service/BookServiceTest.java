package com.guimathis.service;

import com.guimathis.dto.BookIndexResponse;
import com.guimathis.dto.BookRecordDto;
import com.guimathis.dto.ExchangeDTO;
import com.guimathis.environment.InstanceInformationService;
import com.guimathis.model.Book;
import com.guimathis.proxy.ExchangeClient;
import com.guimathis.repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ExchangeClient exchangeClient;

    @Mock
    private InstanceInformationService informationService;

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private BookService bookService;

    @Test
    @DisplayName("Return all books")
    void findAllSuccess() {
        Book book = new Book("Titulo", "Autor", "Editora", 2024, new BigDecimal("100.00"), "USD");
        when(bookRepository.findAll()).thenReturn(List.of(book));

        List<BookIndexResponse> books = bookService.findAll();

        assertThat(books).hasSize(1);
        assertThat(books.getFirst().title()).isEqualTo("Titulo");
    }

    @Test
    @DisplayName("Return book by id successfully")
    void findByIdSuccess() {
        UUID id = UUID.randomUUID();
        Book book = new Book(id, "Titulo", "Autor", "Editora", 2024, new BigDecimal("100.00"), "USD");
        when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        Optional<Book> result = bookService.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Titulo");
    }

    @Test
    @DisplayName("Find book by id with converted currency successfully")
    void findByIdWithCurrencySuccess() {
        UUID id = UUID.randomUUID();
        Book book = new Book(id, "Microservices with Spring", "John Doe", "Tech Publisher"
                , 2024, new BigDecimal("100.00"), "USD");

        ExchangeDTO exchange = new ExchangeDTO();
        exchange.setConvertedValue(new BigDecimal("500.00"));
        exchange.setEnvironment("8000");

        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(exchangeClient.getExchange(new BigDecimal("100.00"), "USD", "BRL")).thenReturn(exchange);
        when(informationService.retrieveHostName()).thenReturn("LOCAL");
        when(informationService.retrieveServerPort()).thenReturn("8100");

        Optional<Book> result = bookService.findByIdWithCurrency(id, "BRL");

        assertThat(result).isPresent();
        Book foundBook = result.get();
        assertThat(foundBook.getPrice()).isEqualTo(new BigDecimal("500.00"));
        assertThat(foundBook.getCurrency()).isEqualTo("BRL");
        assertThat(foundBook.getEnvironment()).isEqualTo("Book-service HOST: LOCAL PORT: 8100 exchange-service HOST: 8000");

        verify(bookRepository).findById(id);
        verify(exchangeClient).getExchange(new BigDecimal("100.00"), "USD", "BRL");
        verify(informationService).retrieveHostName();
        verify(informationService).retrieveServerPort();
    }

    @Test
    @DisplayName("Return empty when book is not found by id with currency")
    void findByIdWithCurrencyBookNotFound() {
        UUID id = UUID.randomUUID();
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Book> result = bookService.findByIdWithCurrency(id, "BRL");

        assertThat(result).isEmpty();

        verify(bookRepository).findById(id);
        verifyNoInteractions(exchangeClient);
        verifyNoInteractions(informationService);
    }

    @Test
    @DisplayName("Save book successfully with generated review")
    void saveSuccess() {
        BookRecordDto bookRecordDto = new BookRecordDto(
                "Clean Code",
                "Robert C. Martin",
                "Prentice Hall",
                2008,
                new BigDecimal("150.00")
        );

        when(reviewService.generateReview("Clean Code")).thenReturn("Review gerada com sucesso");
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book savedBook = bookService.save(bookRecordDto);

        assertThat(savedBook).isNotNull();
        assertThat(savedBook.getTitle()).isEqualTo("Clean Code");
        assertThat(savedBook.getAuthor()).isEqualTo("Robert C. Martin");
        assertThat(savedBook.getPublisher()).isEqualTo("Prentice Hall");
        assertThat(savedBook.getPublicationYear()).isEqualTo(2008);
        assertThat(savedBook.getPrice()).isEqualTo(new BigDecimal("150.00"));
        assertThat(savedBook.getReview()).isEqualTo("Review gerada com sucesso");

        verify(reviewService).generateReview("Clean Code");
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @DisplayName("Update book successfully")
    void updateSuccess() {
        UUID id = UUID.randomUUID();
        Book existingBook = new Book(id, "Old Title", "Old Author", "Old Publisher", 2020, new BigDecimal("80.00"), "USD");
        existingBook.setReview("Existing Review");

        BookRecordDto bookRecordDto = new BookRecordDto(
                "Updated Title",
                "Updated Author",
                "Updated Publisher",
                2024,
                new BigDecimal("120.00")
        );

        when(bookRepository.save(existingBook)).thenReturn(existingBook);

        Book updatedBook = bookService.update(existingBook, bookRecordDto);

        assertThat(updatedBook).isNotNull();
        assertThat(updatedBook.getTitle()).isEqualTo("Updated Title");
        assertThat(updatedBook.getAuthor()).isEqualTo("Updated Author");
        assertThat(updatedBook.getPublisher()).isEqualTo("Updated Publisher");
        assertThat(updatedBook.getPublicationYear()).isEqualTo(2024);
        assertThat(updatedBook.getPrice()).isEqualTo(new BigDecimal("120.00"));

        verify(bookRepository).save(existingBook);
    }

    @Test
    @DisplayName("Delete book successfully")
    void deleteSuccess() {
        UUID id = UUID.randomUUID();
        Book book = new Book(id, "Title", "Author", "Publisher", 2024, new BigDecimal("100.00"), "USD");

        bookService.delete(book);

        verify(bookRepository).delete(book);
    }

    @Test
    @DisplayName("Execute getExchangeFallback successfully when book exists")
    void getExchangeFallbackSuccess() {
        UUID id = UUID.randomUUID();
        Book book = new Book(id, "Spring Boot", "Author", "Publisher", 2024, new BigDecimal("100.00"), "USD");

        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(informationService.retrieveServerPort()).thenReturn("8100");

        Throwable exception = new RuntimeException("Exchange service down");
        Optional<Book> result = ReflectionTestUtils.invokeMethod(bookService, "getExchangeFallback", id, "BRL", exception);

        assertThat(result).isPresent();
        Book fallbackBook = result.get();
        assertThat(fallbackBook.getPrice()).isEqualTo(new BigDecimal("100.00"));
        assertThat(fallbackBook.getCurrency()).isEqualTo("USD");
        assertThat(fallbackBook.getEnvironment()).isEqualTo("Fallback ativado. Causa: Exchange service down. Port: 8100");

        verify(bookRepository).findById(id);
        verify(informationService).retrieveServerPort();
    }

    @Test
    @DisplayName("Return empty in getExchangeFallback when book is not found")
    void getExchangeFallbackBookNotFound() {
        UUID id = UUID.randomUUID();
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        Throwable exception = new RuntimeException("Exchange service down");
        Optional<Book> result = ReflectionTestUtils.invokeMethod(bookService, "getExchangeFallback", id, "BRL", exception);

        assertThat(result).isEmpty();

        verify(bookRepository).findById(id);
        verifyNoInteractions(informationService);
    }
}