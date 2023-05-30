package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
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
import ru.practicum.shareit.booking.dto.BookingDtoResponse;

import javax.validation.Valid;
import java.util.List;

import static ru.practicum.shareit.util.Constants.X_SHARER_USER_ID;

@RestController
@Slf4j
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    BookingDtoResponse bookItem(
            @RequestHeader(value = "X-Sharer-User-Id") long userId,
            @Valid @RequestBody BookingDto booking) {
        log.debug(X_SHARER_USER_ID, userId);
        log.debug("Request received POST '/bookings' : {}", booking);
        return service.bookItem(userId, booking);
    }

    @PatchMapping("/{bookingId}")
    BookingDtoResponse approve(
            @RequestHeader(value = "X-Sharer-User-Id") long userId,
            @PathVariable(name = "bookingId") long bookingId,
            @RequestParam(name = "approved", required = false) boolean isApproved
    ) {
        log.debug(X_SHARER_USER_ID, userId);
        log.debug("Request received PATCH '/bookings/{}?approve={}'", bookingId, isApproved);
        return service.approveBooking(userId, bookingId, isApproved);
    }

    @GetMapping
    List<BookingDtoResponse> getAllByBooker(
            @RequestHeader(value = "X-Sharer-User-Id") long userId,
            @RequestParam(name = "state") String state,
            @RequestParam(name = "from", required = false) Integer from,
            @RequestParam(name = "size", defaultValue = "20") Integer size
    ) {
        log.debug(X_SHARER_USER_ID, userId);
        if (from == null) {
            log.debug("Request received GET '/bookings?state={}'", state);
            return service.getAllByBooker(userId, state, null);
        }
        log.debug("Request received GET '/bookings?state={}&from={}&size={}'", state, from, size);
        final PageRequest page = PageRequest.of(from / size, size);
        return service.getAllByBooker(userId, state, page);
    }

    @GetMapping("/owner")
    List<BookingDtoResponse> getAllByOwner(
            @RequestHeader(value = "X-Sharer-User-Id") long userId,
            @RequestParam(name = "state") String state,
            @RequestParam(name = "from", required = false) Integer from,
            @RequestParam(name = "size", defaultValue = "20") Integer size
    ) {
        log.debug(X_SHARER_USER_ID, userId);
        if (from == null) {
            log.debug("Request received GET '/bookings/owner?state={}'", state);
            return service.getAllByOwner(userId, state, null);
        }
        log.debug("Request received GET '/bookings/owner?state={}&from={}&size={}'", state, from, size);
        final PageRequest page = PageRequest.of(from / size, size);
        return service.getAllByOwner(userId, state, page);
    }

    @GetMapping("/{bookingId}")
    BookingDtoResponse getBookingByIdForUser(@RequestHeader(value = "X-Sharer-User-Id") long userId,
                                             @PathVariable(name = "bookingId") long bookingId) {
        log.debug(X_SHARER_USER_ID, userId);
        log.debug("Request received GET '/bookings/{}'", bookingId);
        return service.getBookingByIdForUser(userId, bookingId);
    }

}
