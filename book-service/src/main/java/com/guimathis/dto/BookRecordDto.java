package com.guimathis.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record BookRecordDto(
        @NotBlank(message = "O Título é obrigatório")
        @Size(max = 150, message = "O Título deve conter no máximo 150 caracteres")
        String title,

        @NotBlank(message = "O Autor é obrigatório")
        @Size(max = 100)
        String author,

        @NotBlank(message = "A Editora é obrigatório")
        @Size(max = 100)
        String publisher,

        @NotNull(message = "O Ano de publicação é obrigatório")
        @Min(value = 1450)
        @Max(value = 2100)
        Integer publicationYear,

        @NotNull(message = "O Preço é obrigatório")
        @PositiveOrZero
        BigDecimal price
) {
}
