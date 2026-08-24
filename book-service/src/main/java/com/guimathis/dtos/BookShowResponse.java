package com.guimathis.dtos;

import com.guimathis.model.Book;

import java.math.BigDecimal;
import java.util.UUID;

public record BookShowResponse(
        UUID id,
        String title,
        String author,
        String publisher,
        Integer publicationYear,
        BigDecimal price,
        String review,
        String currency
) {
    public static BookShowResponse from(Book book) {
        return new BookShowResponse(
                book.getId(), book.getTitle(), book.getAuthor(), book.getPublisher(),
                book.getPublicationYear(), book.getPrice(), book.getReview(), book.getCurrency()
        );
    }
}
