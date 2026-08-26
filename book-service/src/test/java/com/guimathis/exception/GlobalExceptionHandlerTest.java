package com.guimathis.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    @DisplayName("Should return 404 when BookNotFoundException is thrown")
    void handleBookNotFound() {
        UUID id = UUID.randomUUID();
        BookNotFoundException ex = new BookNotFoundException(id);
        when(request.getRequestURI()).thenReturn("/books/" + id);

        ResponseEntity<ErrorResponse> response = handler.handleBookNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("Not Found");
        assertThat(response.getBody().message()).contains(id.toString());
        assertThat(response.getBody().path()).isEqualTo("/books/" + id);
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should return 503 when ReviewGenerationException is thrown")
    void handleReviewGeneration() {
        ReviewGenerationException ex = new ReviewGenerationException("AI review service unavailable");
        when(request.getRequestURI()).thenReturn("/books");

        ResponseEntity<ErrorResponse> response = handler.handleReviewGeneration(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(503);
        assertThat(response.getBody().error()).isEqualTo("Service Unavailable");
        assertThat(response.getBody().message()).isEqualTo("AI review service unavailable");
        assertThat(response.getBody().path()).isEqualTo("/books");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should return 400 when MethodArgumentNotValidException is thrown")
    void handleMethodArgumentNotValid() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("bookRecordDto", "title", "Title is mandatory");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        when(request.getRequestURI()).thenReturn("/books");

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValid(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().message()).contains("title: Title is mandatory");
        assertThat(response.getBody().path()).isEqualTo("/books");
    }

    @Test
    @DisplayName("Should return 400 when MethodArgumentTypeMismatchException is thrown")
    void handleTypeMismatch() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("id");
        when(ex.getRequiredType()).thenReturn((Class) UUID.class);
        when(request.getRequestURI()).thenReturn("/books/invalid-uuid");

        ResponseEntity<ErrorResponse> response = handler.handleTypeMismatch(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().message()).contains("Parameter 'id' should be of type UUID");
        assertThat(response.getBody().path()).isEqualTo("/books/invalid-uuid");
    }

    @Test
    @DisplayName("Should return 400 when HttpMessageNotReadableException is thrown")
    void handleMessageNotReadable() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(request.getRequestURI()).thenReturn("/books");

        ResponseEntity<ErrorResponse> response = handler.handleMessageNotReadable(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().message()).isEqualTo("Malformed JSON request body");
        assertThat(response.getBody().path()).isEqualTo("/books");
    }

    @Test
    @DisplayName("Should return 500 with generic message when unexpected Exception is thrown")
    void handleGeneralException() {
        Exception ex = new RuntimeException("Database connection timeout or internal SQL failure");
        when(request.getRequestURI()).thenReturn("/books");

        ResponseEntity<ErrorResponse> response = handler.handleGeneralException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().error()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred. Please try again later.");
        assertThat(response.getBody().path()).isEqualTo("/books");
        assertThat(response.getBody().timestamp()).isNotNull();
    }
}
