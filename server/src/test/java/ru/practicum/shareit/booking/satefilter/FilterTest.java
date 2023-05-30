package ru.practicum.shareit.booking.satefilter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.practicum.shareit.booking.model.BookingFilter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static ru.practicum.shareit.booking.enums.BookingState.ALL;
import static ru.practicum.shareit.booking.enums.BookingState.CURRENT;
import static ru.practicum.shareit.booking.enums.BookingState.FUTURE;
import static ru.practicum.shareit.booking.enums.BookingState.PAST;
import static ru.practicum.shareit.booking.enums.BookingState.REJECTED;
import static ru.practicum.shareit.booking.enums.BookingState.WAITING;

class FilterTest {

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getFilter_ALL(boolean isUserOwner) {
        final BookingFilter mailFilter = getMainFilter(isUserOwner, 1L);
        final BookingFilter filter = ALL.getStateFilter().getFilter(mailFilter);

        assertNotNull(isUserOwner ? filter.getOwner() : filter.getBooker());

        assertNull(filter.getItem());
        assertNull(filter.getStatus());
        assertNull(filter.getEndBefore());
        assertNull(filter.getEndAfter());
        assertNull(filter.getStartAfter());
        assertNull(filter.getStartBefore());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getFilter_Current(boolean isUserOwner) {
        final BookingFilter buildFilter = getMainFilter(isUserOwner, 1L);
        final BookingFilter filter = CURRENT.getStateFilter().getFilter(buildFilter);

        assertNotNull(isUserOwner ? filter.getOwner() : filter.getBooker());
        assertNotNull(filter.getStartBefore());
        assertNotNull(filter.getEndAfter());

        assertNull(filter.getItem());
        assertNull(filter.getStatus());
        assertNull(filter.getEndBefore());
        assertNull(filter.getStartAfter());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getFilter_Past(boolean isUserOwner) {
        final BookingFilter buildFilter = getMainFilter(isUserOwner, 1L);
        final BookingFilter filter = PAST.getStateFilter().getFilter(buildFilter);

        assertNotNull(isUserOwner ? filter.getOwner() : filter.getBooker());
        assertNotNull(filter.getEndBefore());

        assertNull(filter.getStartBefore());
        assertNull(filter.getStartAfter());
        assertNull(filter.getItem());
        assertNull(filter.getStatus());
        assertNull(filter.getEndAfter());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getFilter_Future(boolean isUserOwner) {
        final BookingFilter buildFilter = getMainFilter(isUserOwner, 1L);
        final BookingFilter filter = FUTURE.getStateFilter().getFilter(buildFilter);

        assertNotNull(isUserOwner ? filter.getOwner() : filter.getBooker());
        assertNotNull(filter.getStartAfter());

        assertNull(filter.getStartBefore());
        assertNull(filter.getEndAfter());
        assertNull(filter.getItem());
        assertNull(filter.getStatus());
        assertNull(filter.getEndBefore());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getFilter_Rejected(boolean isUserOwner) {
        final BookingFilter buildFilter = getMainFilter(isUserOwner, 1L);
        final BookingFilter filter = REJECTED.getStateFilter().getFilter(buildFilter);

        assertNotNull(isUserOwner ? filter.getOwner() : filter.getBooker());
        assertNotNull(filter.getStatus());

        assertNull(filter.getStartBefore());
        assertNull(filter.getEndAfter());
        assertNull(filter.getItem());
        assertNull(filter.getEndBefore());
        assertNull(filter.getStartAfter());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getFilter_Waiting(boolean isUserOwner) {
        final BookingFilter buildFilter = getMainFilter(isUserOwner, 1L);
        final BookingFilter filter = WAITING.getStateFilter().getFilter(buildFilter);

        assertNotNull(isUserOwner ? filter.getOwner() : filter.getBooker());
        assertNotNull(filter.getStatus());

        assertNull(filter.getStartBefore());
        assertNull(filter.getEndAfter());
        assertNull(filter.getItem());
        assertNull(filter.getEndBefore());
        assertNull(filter.getStartAfter());
    }

    private static BookingFilter getMainFilter(boolean isUserOwner, long userId) {
        return BookingFilter.builder()
                .owner(isUserOwner ? userId : null)
                .booker(isUserOwner ? null : userId)
                .build();
    }
}