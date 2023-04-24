package ru.practicum.shareit.booking.dto;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.BookingStatus;

import java.util.Date;

/**
 * TODO Sprint add-bookings.
 */
@Setter
@Getter
public class BookingDto {
    private long id;// уникальный идентификатор
    private Date start;// дата и время начала бронирования
    private Date end;// дата и время конца бронирования
    private long item;// вещь, которую пользователь бронирует
    private long booker;// пользователь, который осуществляет бронирование
    private BookingStatus status;// статус бронирования.
}
