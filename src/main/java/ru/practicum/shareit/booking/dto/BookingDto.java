package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDtoShort;
import ru.practicum.shareit.user.dto.UserShort;

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
    private long id;// уникальный идентификатор

    @NotNull(message = "ItemId cannot be empty or null")
    @Positive(message = "ItemId must be positive")
    private Long itemId;// вещь, которую пользователь бронирует

    @NotNull(message = "Booking start date cannot be empty or null")
    @FutureOrPresent(message = "Booking start date must not be in the past")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime start;// дата и время начала бронирования

    @NotNull(message = "Booking end date cannot be empty or null")
    @FutureOrPresent(message = "Booking end date must not be in the past")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime end;// дата и время конца бронирования

    private BookingStatus status;// статус бронирования.
    private ItemDtoShort item;
    private UserShort booker;
}
