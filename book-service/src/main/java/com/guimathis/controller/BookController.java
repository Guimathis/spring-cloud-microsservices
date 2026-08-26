package com.guimathis.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guimathis.dto.BookIndexResponse;
import com.guimathis.dto.BookRecordDto;
import com.guimathis.dto.BookShowResponse;
import com.guimathis.exception.BookNotFoundException;
import com.guimathis.model.Book;
import com.guimathis.service.BookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Books", description = "Operations related to books")
@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @Operation(summary = "Create a new book", description = "Creates a new book from the provided input data")
    @PostMapping(version = "v1")
    public ResponseEntity<Book> store(@RequestBody @Valid BookRecordDto bookRecordDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.save(bookRecordDto));
    }

    @Operation(summary = "Update an existing book", description = "Updates a book by its unique identifier", parameters = {
            @Parameter(name = "id", description = "Unique identifier of the book to update", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
    })
    @PutMapping(value = "/{id}", version = "v1")
    public ResponseEntity<Book> update(@PathVariable(value = "id") UUID id,
                                         @RequestBody @Valid BookRecordDto bookRecordDto) {
        Book book = bookService.findById(id).orElseThrow(() -> new BookNotFoundException(id));
        return ResponseEntity.status(HttpStatus.OK).body(bookService.update(book, bookRecordDto));
    }

    @Operation(summary = "Delete a book", description = "Deletes a book by its unique identifier", parameters = {
            @Parameter(name = "id", description = "Unique identifier of the book to delete", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
    })
    @DeleteMapping(value = "/{id}", version = "v1")
    public ResponseEntity<Void> destroy(@PathVariable(value = "id") UUID id) {
        Book book = bookService.findById(id).orElseThrow(() -> new BookNotFoundException(id));

        bookService.delete(book);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Find a book by id and currency", description = "Finds a book by its identifier and converts its price to the specified currency", parameters = {
            @Parameter(name = "id", description = "Unique identifier of the book", required = true, example = "123e4567-e89b-12d3-a456-426614174000"),
            @Parameter(name = "currency", description = "Currency code to convert the price to", required = true, example = "USD")
    })
    @GetMapping(value = "/{id}/{currency}", version = "v1")
    public ResponseEntity<Book> showBookWithCurrency(@PathVariable(value = "id") UUID id,
                                                       @PathVariable(value = "currency") String currency) {
        Book book = bookService.findByIdWithCurrency(id, currency).orElseThrow(() -> new BookNotFoundException(id));

        return ResponseEntity.status(HttpStatus.OK).body(book);
    }

    @Operation(summary = "Find a book by id", description = "Finds a book by its identifier", parameters = {
            @Parameter(name = "id", description = "Unique identifier of the book", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
    })
    @GetMapping(value = "/{id}", version = "v1")
    public ResponseEntity<BookShowResponse> show(@PathVariable UUID id) {
        Book book = bookService.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));

        return ResponseEntity.ok(BookShowResponse.from(book));
    }

    @Operation(summary = "Find all books", description = "Finds all books")
    @GetMapping(version = "v1")
    public ResponseEntity<List<BookIndexResponse>> index() {
        List<BookIndexResponse> books = bookService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(books);
    }
}