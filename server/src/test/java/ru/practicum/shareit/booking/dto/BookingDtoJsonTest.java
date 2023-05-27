package ru.practicum.shareit.booking.dto;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.enums.BookingStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class BookingDtoJsonTest {
    @Autowired
    private JacksonTester<BookingDtoResponse> json;

    @SneakyThrows
    @Test
    void testBookingDto() {
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        BookingDtoResponse dto = BookingDtoResponse.builder()
                .id(1L)
                .status(BookingStatus.WAITING)
                .start(currentTime.plusDays(1))
                .end(currentTime.plusDays(3))
                .itemId(1L)
                .build();

        JsonContent<BookingDtoResponse> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo((currentTime.plusDays(1)).format(formatter));
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo((currentTime.plusDays(3)).format(formatter));
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
    }
}