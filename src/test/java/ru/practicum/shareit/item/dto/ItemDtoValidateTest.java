package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.practicum.shareit.TestValidatorUtil.hasErrorMessage;

class ItemDtoValidateTest {

    @ParameterizedTest
    @CsvSource({",'   '", "''"})
    void create_whenWrongName(String name) {
        final ItemDto itemDto = ItemDto.builder()
                .name(name)
                .description("Description")
                .available(true)
                .build();
        assertTrue(hasErrorMessage(itemDto, "Name cannot be empty or null"));
    }

    @ParameterizedTest
    @CsvSource({",'   '", "''"})
    void create_whenWrongDescription(String description) {
        final ItemDto itemDto = ItemDto.builder()
                .name("name")
                .description(description)
                .available(true)
                .build();
        assertTrue(hasErrorMessage(itemDto, "Description cannot be empty or null"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "                "})
    void create_whenWrongDescriptionAndAvailable(String description) {
        final ItemDto itemDto = ItemDto.builder()
                .name("name")
                .description(description)
                .build();
        Assertions.assertAll(
                () -> assertTrue(hasErrorMessage(itemDto, "Available cannot be null")),
                () -> assertTrue(hasErrorMessage(itemDto, "Description cannot be empty or null"))
        );
    }
}