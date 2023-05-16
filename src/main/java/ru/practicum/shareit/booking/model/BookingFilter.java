package ru.practicum.shareit.booking.model;

import lombok.Builder;
import lombok.Getter;
import ru.practicum.shareit.util.QFilter;
import ru.practicum.shareit.booking.enums.BookingStatus;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
public class BookingFilter implements QFilter {
    private Long booker;
    private Long owner;
    private Long item;
    private LocalDateTime startBefore;
    private LocalDateTime startAfter;
    private LocalDateTime endBefore;
    private LocalDateTime endAfter;
    private BookingStatus status;
}
