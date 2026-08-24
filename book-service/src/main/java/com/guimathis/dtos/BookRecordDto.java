package com.guimathis.dtos;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record BookRecordDto(
        @NotBlank
        @Size(max = 150)
        String title,

        @NotBlank
        @Size(max = 100)
        String author,

        @NotBlank
        @Size(max = 100)
        String publisher,

        @NotNull
        @Min(value = 1450)
        @Max(value = 2100)
        Integer publicationYear,

        @NotNull
        @PositiveOrZero
        BigDecimal price
) {
}
