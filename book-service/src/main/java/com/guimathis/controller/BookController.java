package com.guimathis.controller;

import com.guimathis.model.Book;
import com.guimathis.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Book endpoint", description = "Operations related to books")
@RestController
@RequestMapping("/book-service")
public class BookController {

    @Autowired
    BookService bookService;

    // http://localhost:8100/book/1/BRL
    @Operation(summary = "Find a book by id and currency", description = "Find a book by id and currency", parameters = {
            @Parameter(name = "id", description = "Book id", required = true, example = "1"),
            @Parameter(name = "currency", description = "Currency code", required = true, example = "BRL")
    })
    @GetMapping(value = "/{id}/{currency}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Book findBook(@PathVariable("id") Long id, @PathVariable("currency") String currency) {
        return bookService.findBook(id, currency);
    }

}