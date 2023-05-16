package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.practicum.shareit.TestValidatorUtil.hasErrorMessage;

class BookingDtoValidateTest {
    @Test
    void create_whenDatesInPast() {
        final LocalDateTime start = LocalDateTime.now().minusDays(1);
        final LocalDateTime end = LocalDateTime.now().minusDays(2);
        final BookingDto bookingDto = BookingDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        Assertions.assertAll(
                () -> assertTrue(hasErrorMessage(bookingDto, "Booking start date must not be in the past")),
                () -> assertTrue(hasErrorMessage(bookingDto, "Booking end date must not be in the past"))
        );
    }

    @Test
    void create_whenDatesIsNull() {
        final BookingDto bookingDto = BookingDto.builder()
                .itemId(1L)
                .build();

        Assertions.assertAll(
                () -> assertTrue(hasErrorMessage(bookingDto, "Booking start date cannot be empty or null")),
                () -> assertTrue(hasErrorMessage(bookingDto, "Booking end date cannot be empty or null"))
        );
    }

    @ParameterizedTest
    @CsvSource({",ItemId cannot be empty or null", "0, ItemId must be positive"})
    void create_whenItemIdIsNull(Long itemId, String expectedMessage) {
        final LocalDateTime start = LocalDateTime.now().minusDays(1);
        final LocalDateTime end = LocalDateTime.now().minusDays(2);
        final BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();
        assertTrue(hasErrorMessage(bookingDto, expectedMessage));
    }
}