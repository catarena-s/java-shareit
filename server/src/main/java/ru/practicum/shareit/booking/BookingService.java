package ru.practicum.shareit.booking;

import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;

import java.util.List;

public interface BookingService {
    /**
     * Create booking
     * @param userId
     * @param booking
     * @return
     */
    BookingDtoResponse bookItem(long userId, BookingDto booking);

    /**
     * Update approve status for booking
     * @param userId
     * @param bookingId
     * @param isApproved
     * @return
     */
    BookingDtoResponse approveBooking(long userId, long bookingId, boolean isApproved);

    /**
     * Get Booking by id and userID and bookingId
     * @param userId
     * @param bookingId
     * @return
     */
    BookingDtoResponse getBookingByIdForUser(long userId, long bookingId);

    /**
     * Get bookings fo item's owner and state
     * @param userId
     * @param state
     * @return
     */
    List<BookingDtoResponse> getAllByOwner(long userId, String state, PageRequest page);

    /**
     * Get all bookings for user and state
     * @param userId
     * @param state
     * @param page
     * @return
     */
    List<BookingDtoResponse> getAllByBooker(long userId, String state, PageRequest page);
}
