package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BookingDto {
    @NotNull(message = "ItemId cannot be empty or null")
    @Positive(message = "ItemId must be positive")
    private Long itemId;// вещь, которую пользователь бронирует

    @NotNull(message = "Booking start date cannot be empty or null")
    @FutureOrPresent(message = "Booking start date must not be in the past")
    private LocalDateTime start;// дата и время начала бронирования

    @NotNull(message = "Booking end date cannot be empty or null")
    @FutureOrPresent(message = "Booking end date must not be in the past")
    private LocalDateTime end;// дата и время конца бронирования
}
