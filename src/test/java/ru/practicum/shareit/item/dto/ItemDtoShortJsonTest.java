package ru.practicum.shareit.item.dto;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
@JsonTest
class ItemDtoShortJsonTest {
    @Autowired
    private JacksonTester<ItemDtoShort> json;

    @SneakyThrows
    @Test
    void testItemDtoShort() {
        ItemDtoShort itemDto = ItemDtoShort.builder()
                .id(1)
                .name("Item name")
                .description("item description")
                .available(true)
                .requestId(1L)
                .build();

        JsonContent<ItemDtoShort> result = json.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(itemDto.getName());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(itemDto.getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(itemDto.isAvailable());
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(1);
    }
}