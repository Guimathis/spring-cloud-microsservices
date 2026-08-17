package com.guimathis;

import com.guimathis.environment.InstanceInformationService;
import com.guimathis.model.Book;
import com.guimathis.proxy.ExchangeClient;
import com.guimathis.repository.BookRepository;
import com.guimathis.service.BookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ExchangeClient exchangeClient;

    @Mock
    private InstanceInformationService informationService;

    @InjectMocks
    private BookService bookService;

    @Test
    void shouldReturnFallbackWhenExceptionOccurs() {
        // Arrange
        Long bookId = 1L;
        String currency = "BRL";
        Book book = new Book();
        book.setId(bookId);
        book.setPrice(100.0);
        
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(exchangeClient.getExchange(anyDouble(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Exchange not supported for USD to USD"));
        when(informationService.retrieveServerPort()).thenReturn("8100");

        // Act
        // O resilience4j não é ativado em testes unitários puros sem o AOP do Spring, 
        // mas podemos testar o método de fallback diretamente ou via reflexão se necessário.
        // Como o objetivo é validar a lógica do fallback:
        
        // Simular a chamada ao fallback que o Resilience4j faria
        try {
            bookService.findBook(bookId, currency);
        } catch (Exception e) {
            Book result = invokeFallback(bookId, currency, e);
            
            // Assert
            assertTrue(result.getEnvironment().contains("Fallback ativado. Causa: Exchange not supported for USD to USD"));
            assertEquals(100.0, result.getPrice());
        }
    }

    private Book invokeFallback(Long id, String currency, Throwable e) {
        // Simulação manual da chamada que o R4J faria ao método privado
        try {
            var method = BookService.class.getDeclaredMethod("getExchangeFallback", Long.class, String.class, Throwable.class);
            method.setAccessible(true);
            return (Book) method.invoke(bookService, id, currency, e);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
