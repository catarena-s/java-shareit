package ru.practicum.shareit.booking;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * TODO Sprint add-bookings.
 */
@Setter
@Getter
@ToString
@EqualsAndHashCode
public class Booking {
    private long id;// уникальный идентификатор
    private Date start;// дата и время начала бронирования
    private Date end;// дата и время конца бронирования
    private long item;// вещь, которую пользователь бронирует
    private long booker;// пользователь, который осуществляет бронирование
    private BookingStatus status;// статус бронирования.
}
