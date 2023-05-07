package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingDto;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@Slf4j
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    BookingDto create(
            @RequestHeader(value = "X-Sharer-User-Id", required = false) long userId,
            @Valid @RequestBody BookingDto booking) {
        log.debug("Request received POST '/bookings' : {}", booking);
        log.debug("X-Sharer-User-Id={}", userId);
        return service.createBooking(userId, booking);
    }

    @PatchMapping("/{bookingId}")
    BookingDto approve(
            @RequestHeader(value = "X-Sharer-User-Id", required = false) long userId,
            @PathVariable(name = "bookingId") long bookingId,
            @RequestParam(name = "approved", required = false) boolean isApproved
    ) {
        log.debug("Request received PATCH '/bookings/{}?approve={}'", bookingId, isApproved);
        log.debug("X-Sharer-User-Id={}", userId);
        return service.approveBooking(userId, bookingId, isApproved);
    }

    @GetMapping
    List<BookingDto> getAllByBooker(
            @RequestHeader(value = "X-Sharer-User-Id", required = false) long userId,
            @RequestParam(name = "state", defaultValue = "ALL", required = false) String state
    ) {
        log.debug("Request received GET '/bookings?state={}'", state);
        log.debug("X-Sharer-User-Id={}", userId);
        return service.getAllByBooker(userId, state);
    }

    @GetMapping("/owner")
    List<BookingDto> getAllByOwner(@RequestHeader(value = "X-Sharer-User-Id", required = false) long userId,
                                   @RequestParam(name = "state", defaultValue = "ALL", required = false) String state) {
        log.debug("Request received GET '/bookings/owner?state={}'", state);
        log.debug("X-Sharer-User-Id={}", userId);
        return service.getAllByOwner(userId, state);
    }

    @GetMapping("/{bookingId}")
    BookingDto getBookingByIdForUser(@RequestHeader(value = "X-Sharer-User-Id", required = false) long userId,
                                     @PathVariable(name = "bookingId") long bookingId) {
        log.debug("Request received GET '/bookings/{}'", bookingId);
        log.debug("X-Sharer-User-Id={}", userId);
        return service.getBookingByIdForUser(userId, bookingId);
    }
}
