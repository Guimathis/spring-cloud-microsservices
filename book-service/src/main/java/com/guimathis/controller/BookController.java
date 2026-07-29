package com.guimathis.controller;

import com.guimathis.model.Book;
import com.guimathis.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/book")
public class BookController {

    @Autowired
    BookService bookService;

    // http://localhost:8100/book/1/BRL
    @GetMapping(value = "/{id}/{currency}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Book findBook(@PathVariable("id") Long id, @PathVariable("currency") String currency) {
        return bookService.findBook(id, currency);
    }

}