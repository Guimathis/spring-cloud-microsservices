package com.guimathis.controller;

import com.guimathis.dtos.BookIndexResponse;
import com.guimathis.dtos.BookRecordDto;
import com.guimathis.dtos.BookShowResponse;
import com.guimathis.exception.BookNotFoundException;
import com.guimathis.model.Book;
import com.guimathis.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @Operation(summary = "Update an existing book", description = "Updates a book by its unique identifier")
    @PutMapping(value = "/{id}", version = "v1")
    public ResponseEntity<Book> update(@PathVariable(value = "id") UUID id,
                                         @RequestBody @Valid BookRecordDto bookRecordDto) {
       Book book = bookService.findById(id).orElseThrow(() -> new BookNotFoundException(id));
        return ResponseEntity.status(HttpStatus.OK).body(bookService.update(book, bookRecordDto));
    }

    @Operation(summary = "Delete a book", description = "Deletes a book by its unique identifier")
    @DeleteMapping(value = "/{id}", version = "v1")
    public ResponseEntity<Void> destroy(@PathVariable(value = "id") UUID id) {
        Book book = bookService.findById(id).orElseThrow(() -> new BookNotFoundException(id));

        bookService.delete(book);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Find a book by id and currency", description = "Finds a book by its identifier and converts its price to the specified currency")
    @GetMapping(value = "/{id}/{currency}", version = "v1")
    public ResponseEntity<Book> showBookWithCurrency(@PathVariable(value = "id") UUID id,
                                                       @PathVariable(value = "currency") String currency) {
        Book book = bookService.findById(id, currency).orElseThrow(() -> new BookNotFoundException(id));

        return ResponseEntity.status(HttpStatus.OK).body(book);
    }

    @Operation(summary = "Find a book by id", description = "Finds a book by its identifier")
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