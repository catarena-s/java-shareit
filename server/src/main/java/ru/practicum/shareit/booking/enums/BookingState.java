package ru.practicum.shareit.booking.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.booking.model.BookingFilter;
import ru.practicum.shareit.booking.satefilter.All;
import ru.practicum.shareit.booking.satefilter.Current;
import ru.practicum.shareit.booking.satefilter.Future;
import ru.practicum.shareit.booking.satefilter.PastBookings;
import ru.practicum.shareit.booking.satefilter.Rejected;
import ru.practicum.shareit.util.Filter;
import ru.practicum.shareit.booking.satefilter.Waiting;

import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum BookingState {
    ALL(new All()),
    CURRENT(new Current()),//текущие
    PAST(new PastBookings()),//завершённые
    FUTURE(new Future()),//будущие
    WAITING(new Waiting()),//ожидающие подтверждения
    REJECTED(new Rejected());//отклонённые

    private final Filter<BookingFilter> stateFilter;

    public static Optional<BookingState> from(String stringState) {
        for (BookingState state : values()) {
            if (state.name().equalsIgnoreCase(stringState)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }
}
