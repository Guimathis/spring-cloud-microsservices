package com.guimathis.exchangeservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    @DisplayName("Should return 400 when ExchangeNotSuportedException is thrown")
    void handleExchangeNotSupportedException() {
        ExchangeNotSuportedException ex = new ExchangeNotSuportedException("USD", "EUR");
        when(request.getRequestURI()).thenReturn("/exchange-service/100/USD/EUR");

        ResponseEntity<ErrorResponse> response = handler.handleExchangeNotSupportedException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals("Exchange not supported for USD to EUR", response.getBody().message());
        assertEquals("/exchange-service/100/USD/EUR", response.getBody().path());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    @DisplayName("Should return 500 with generic message when unexpected Exception is thrown")
    void handleGeneralException() {
        Exception ex = new RuntimeException("Database error");
        when(request.getRequestURI()).thenReturn("/exchange-service/100/USD/EUR");

        ResponseEntity<ErrorResponse> response = handler.handleGeneralException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().status());
        assertEquals("Internal Server Error", response.getBody().error());
        assertEquals("An unexpected error occurred. Please try again later.", response.getBody().message());
        assertEquals("/exchange-service/100/USD/EUR", response.getBody().path());
        assertNotNull(response.getBody().timestamp());
    }
}
