package ru.practicum.shareit.item.dto;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.practicum.shareit.TestValidatorUtil.hasErrorMessage;

class CommentDtoValidateTest {
    @ParameterizedTest
    @CsvSource({",'   '", "''"})
    void create_whenTextEmpty(String text) {
        final CommentDto commentDto = CommentDto.builder()
                .text(text)
                .build();

        assertTrue(hasErrorMessage(commentDto, "Text cannot be empty or null"));
    }
}