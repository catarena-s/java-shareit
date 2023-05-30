package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.exception.ValidateException;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import static ru.practicum.shareit.util.Constants.X_SHARER_USER_ID;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @GetMapping
    public ResponseEntity<Object> getAllByBooker(
            @RequestHeader(value = "X-Sharer-User-Id") long userId,
            @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
            @RequestParam(name = "from", required = false) @Min(0) Integer from,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(50) Integer size
    ) {
        final BookingState state = getBookingState(stateParam);
        log.debug(X_SHARER_USER_ID, userId);
        if (from == null) {
            log.debug("Request received GET '/bookings?state={}'", state);
            return bookingClient.getBookings(userId, state);
        }
        log.debug("Request received GET '/bookings?state={}&from={}&size={}'", state, from, size);
        return bookingClient.getBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllByOwner(
            @RequestHeader(value = "X-Sharer-User-Id") long userId,
            @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
            @RequestParam(name = "from", required = false) @Min(0) Integer from,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(50) Integer size
    ) {
        final BookingState state = getBookingState(stateParam);
        log.debug(X_SHARER_USER_ID, userId);
        if (from == null) {
            log.debug("Request received GET '/bookings/owner?state={}'", state);
            return bookingClient.getAllByOwner(userId, state);
        }
        log.debug("Request received GET '/bookings/owner?state={}&from={}&size={}'", state, from, size);
        return bookingClient.getAllByOwner(userId, state, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> bookItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                           @RequestBody @Valid BookingDto requestDto) {
        log.debug(X_SHARER_USER_ID, userId);
        log.info("Creating booking {}", requestDto);
        if (!isStartBeforeEnd(requestDto)) {
            throw new ValidateException("Booking start date must be before end date.");
        }
        return bookingClient.bookItem(userId, requestDto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingByIdForUser(@RequestHeader(value = "X-Sharer-User-Id") long userId,
                                                        @PathVariable(name = "bookingId") long bookingId) {
        log.debug("Request received GET '/bookings/{}'", bookingId);
        log.debug(X_SHARER_USER_ID, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approve(
            @RequestHeader(value = "X-Sharer-User-Id") long userId,
            @PathVariable(name = "bookingId") long bookingId,
            @RequestParam(name = "approved", required = false) boolean isApproved
    ) {
        log.debug("Request received PATCH '/bookings/{}?approved={}'", bookingId, isApproved);
        log.debug(X_SHARER_USER_ID, userId);
        return bookingClient.setApproveStatus(userId, bookingId, isApproved);
    }

    private boolean isStartBeforeEnd(BookingDto bookingDto) {
        return bookingDto.getStart().isBefore(bookingDto.getEnd());
    }


    private static BookingState getBookingState(String stateParam) {
        return BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
    }
}
