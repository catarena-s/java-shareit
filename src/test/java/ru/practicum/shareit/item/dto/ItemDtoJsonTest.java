package ru.practicum.shareit.item.dto;

import lombok.SneakyThrows;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class ItemDtoJsonTest {
    @Autowired
    private JacksonTester<ItemDto> json;

    @SneakyThrows
    @Test
    void testUserDto() {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Item name")
                .description("item description")
                .available(true)
                .requestId(1L)
                .build();

        JsonContent<ItemDto> result = json.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(itemDto.getName());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(itemDto.getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(itemDto.getAvailable());
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(1);
        assertThat(result).hasEmptyJsonPathValue("$.lastBooking");
        assertThat(result).hasEmptyJsonPathValue("$.nextBooking");
        assertThat(result).hasEmptyJsonPathValue("$.comments");
    }

    @SneakyThrows
    @Test
    void testUserDto_withoutRequest() {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Item name")
                .description("item description")
                .available(true)
                .comments(Collections.emptyList())
                .build();

        JsonContent<ItemDto> result = json.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(itemDto.getName());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(itemDto.getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(itemDto.getAvailable());
        assertThat(result).doesNotHaveJsonPath("$.requestId");
        assertThat(result).hasEmptyJsonPathValue("$.lastBooking");
        assertThat(result).hasEmptyJsonPathValue("$.nextBooking");
        assertThat(result).hasEmptyJsonPathValue("$.comments");
        assertThat(result).extractingJsonPathArrayValue("$.comments").isEmpty();
    }

    @SneakyThrows
    @Test
    void testUserDto_withBookingAndComment() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

        BookingShortDto lastBooking = BookingShortDto.builder()
                .id(2L)
                .bookerId(3L)
                .start(LocalDateTime.of(2023, 1, 15, 12, 5))
                .build();
        final LocalDateTime currentTime = LocalDateTime.now();
        List<CommentDto> comments = List.of(
                CommentDto.builder().id(1L).itemId(1L).text("Comment 1").authorName("user1").created(currentTime.minusDays(2)).build(),
                CommentDto.builder().id(2L).itemId(5L).text("Comment 2").authorName("user2").created(currentTime.minusDays(6)).build()
        );
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Item name")
                .description("item description")
                .available(true)
                .requestId(1L)
                .lastBooking(lastBooking)
                .comments(comments)
                .build();

        JsonContent<ItemDto> result = json.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(itemDto.getName());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(itemDto.getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(itemDto.getAvailable());
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(1);

        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.id").isEqualTo(2);
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.bookerId").isEqualTo(3);
        assertThat(result).extractingJsonPathStringValue("$.lastBooking.start").isEqualTo("2023-01-15T12:05:00");

        assertThat(result).hasEmptyJsonPathValue("$.nextBooking");

        final ListAssert<Object> commentsListAssert = assertThat(result).extractingJsonPathArrayValue("$.comments");
        commentsListAssert.hasSize(2);

        final ObjectAssert<Object> comment1 = commentsListAssert.element(0);
        comment1.hasFieldOrPropertyWithValue("id", 1);
        comment1.hasFieldOrPropertyWithValue("text", "Comment 1");
        comment1.hasFieldOrPropertyWithValue("created", comments.get(0).getCreated().format(formatter));
        comment1.hasFieldOrPropertyWithValue("authorName", "user1");
        comment1.doesNotHaveToString("itemId");

        final ObjectAssert<Object> comment2 = commentsListAssert.element(1);
        comment2.hasFieldOrPropertyWithValue("id", 2);
        comment2.hasFieldOrPropertyWithValue("text", "Comment 2");
        comment2.hasFieldOrPropertyWithValue("created", comments.get(1).getCreated().format(formatter));
        comment2.hasFieldOrPropertyWithValue("authorName", "user2");
        comment2.doesNotHaveToString("itemId");
    }
}