package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    /**
     * Create booking
     * @param userId
     * @param booking
     * @return
     */
    BookingDto createBooking(long userId, BookingDto booking);

    /**
     * Update approve status for booking
     * @param userId
     * @param bookingId
     * @param isApproved
     * @return
     */
    BookingDto approveBooking(long userId, long bookingId, boolean isApproved);

    /**
     * Get all bookings for user
     * @param userId
     * @param state
     * @return
     */
    List<BookingDto> getAllByBooker(long userId, String state);

    /**
     * Get Booking by id and userID and state
     * @param userId
     * @param bookingId
     * @return
     */
    BookingDto getBookingByIdForUser(long userId, long bookingId);

    /**
     * Get bookings fo item's owner and state
     * @param userId
     * @param state
     * @return
     */
    List<BookingDto> getByItemsOwner(long userId, String state);
}