package com.guimathis.dto;

import com.guimathis.model.Book;

import java.math.BigDecimal;
import java.util.UUID;

public record BookIndexResponse(
        UUID id,
        String title,
        String author,
        String publisher,
        Integer publicationYear,
        BigDecimal price,
        String review,
        String currency
) {
    public BookIndexResponse(Book book) {
       this(book.getId(), book.getTitle(), book.getAuthor(), book.getPublisher(), book.getPublicationYear(),
                book.getPrice(), book.getReview(), book.getCurrency());
    }
}
