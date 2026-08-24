package com.guimathis.dtos;

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
}
