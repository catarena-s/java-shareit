package ru.practicum.shareit.request.dto;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.practicum.shareit.TestValidatorUtil.hasErrorMessage;

class ItemRequestDtoValidateTest {

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

}