package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDtoShort;
import ru.practicum.shareit.user.dto.UserShort;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Setter
@Getter
@Builder
public class BookingDto {
    private long id;// уникальный идентификатор
    @NotNull
    private long itemId;// вещь, которую пользователь бронирует
    @NotNull
    @FutureOrPresent(message = "Booking start date must not be in the past")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime start;// дата и время начала бронирования
    @NotNull
    @FutureOrPresent(message = "Booking end date must not be in the past")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime end;// дата и время конца бронирования

    private BookingStatus status;// статус бронирования.
    private ItemDtoShort item;
    private UserShort booker;
}
