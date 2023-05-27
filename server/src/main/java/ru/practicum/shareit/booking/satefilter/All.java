package ru.practicum.shareit.booking.satefilter;

import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.BookingFilter;
import ru.practicum.shareit.util.Filter;

@NoArgsConstructor
public class All implements Filter<BookingFilter> {
    @Override
    public BookingFilter getFilter(BookingFilter filter) {
        return filter;
    }
}
