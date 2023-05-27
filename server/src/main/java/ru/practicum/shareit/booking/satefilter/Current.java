package ru.practicum.shareit.booking.satefilter;

import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.BookingFilter;
import ru.practicum.shareit.util.Filter;

import java.time.LocalDateTime;

@NoArgsConstructor
public class Current implements Filter<BookingFilter> {
    @Override
    public BookingFilter getFilter(BookingFilter filter) {
        final LocalDateTime currentTime = LocalDateTime.now();
        return filter.toBuilder()
                .startBefore(currentTime)
                .endAfter(currentTime)
                .build();
    }
}
