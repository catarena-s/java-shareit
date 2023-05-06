package ru.practicum.shareit.booking;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingFilter;
import ru.practicum.shareit.booking.dto.BookingMapping;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exceptions.AvailableException;
import ru.practicum.shareit.exceptions.NoAccessException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.RequestException;
import ru.practicum.shareit.exceptions.ValidateException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.util.Constants;
import ru.practicum.shareit.util.QPredicate;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.shareit.booking.model.QBooking.booking;
import static ru.practicum.shareit.util.Constants.MSG_ITEM_WITH_ID_NOT_FOUND;
import static ru.practicum.shareit.util.Constants.MSG_USER_WITH_ID_NOT_FOUND;
import static ru.practicum.shareit.util.Constants.SORT_BY_START_DESC;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final UserService userService;
    private final BookingRepository repository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingDto createBooking(long userId, BookingDto bookingDto) {
        if (!isStartBeforeEnd(bookingDto)) {
            throw new ValidateException("Booking start date must be before end date.");
        }
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException(String.format(MSG_USER_WITH_ID_NOT_FOUND, userId)));

        Item item = itemRepository.findById(bookingDto.getItemId()).orElseThrow(
                () -> new NotFoundException(String.format(MSG_ITEM_WITH_ID_NOT_FOUND, bookingDto.getItemId())));

        if (item.getOwner().getId() == userId) {
            throw new NotFoundException("The owner cannot book his item");
        }

        if (!item.isAvailable()) {
            throw new AvailableException(String.format("Item with id=%d is not available", bookingDto.getItemId()));
        }

        if (repository.existsApprovedBookingForItemWithCrossTime(bookingDto.getItemId(), bookingDto.getStart(), bookingDto.getEnd())) {
            throw new AvailableException(String.format(
                    "Booking item(id=%d) is not available for dates from %s to %s",
                    bookingDto.getItemId(), bookingDto.getStart(), bookingDto.getEnd()
            ));
        }

        Booking booking = repository.save(BookingMapping.toBooking(bookingDto, item, user));
        return BookingMapping.toDto(booking);
    }

    @Override
    @Transactional
    public BookingDto approveBooking(long userId, long bookingId, boolean isApproved) {
        Booking booking = repository.findById(bookingId).orElseThrow(
                () -> new NotFoundException(String.format(Constants.MSG_BOOKING_WITH_ID_NOT_FOUND, bookingId)));

        if (booking.getBooker().getId() == userId) {
            throw new NotFoundException("Booker cannot change booking status");
        }
        if (!BookingStatus.WAITING.equals(booking.getStatus())) {
            throw new RequestException("The booking status should be 'WAITING'");
        }
        if (booking.getItem().getOwner().getId() != userId) {
            throw new NoAccessException(String.format("The user(id=%d) does not have access to approve booking.", userId));
        }
        if (booking.getEnd().isBefore(LocalDateTime.now())) {
            throw new NoAccessException("You cannot confirm a booking that has already expired");
        }
        if (repository.existsApprovedBookingForItemWithCrossTime(booking.getItem().getId(), booking.getStart(), booking.getEnd())) {
            throw new AvailableException(String.format(
                    "Booking(id=%d) for item(id=%d) is not available for approve for dates from %s to %s",
                    bookingId, booking.getItem().getId(), booking.getStart(), booking.getEnd()
            ));
        }

        booking.setStatus(isApproved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = repository.save(booking);
        return BookingMapping.toDto(updatedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto getBookingByIdForUser(long userId, long bookingId) {
        if (!userService.existUser(userId)) {
            throw new NotFoundException(String.format(MSG_USER_WITH_ID_NOT_FOUND, userId));
        }
        Booking booking = repository.findByIdAndBookerOrOwner(userId, bookingId).orElseThrow(
                () -> new NotFoundException(String.format(Constants.MSG_BOOKING_WITH_ID_NOT_FOUND, bookingId)));
        return BookingMapping.toDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getAllByBooker(long userId, String state) {
        Iterable<Booking> bookings = getBookingsByUserAndState(userId, state, false);
        return BookingMapping.toListDto(bookings);

    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getAllByOwner(long userId, String state) {
        Iterable<Booking> bookings = getBookingsByUserAndState(userId, state, true);
        return BookingMapping.toListDto(bookings);

    }

    @NotNull
    private Iterable<Booking> getBookingsByUserAndState(long userId, String state, boolean isUserOwner) {
        if (!userService.existUser(userId)) {
            throw new NotFoundException(String.format(MSG_USER_WITH_ID_NOT_FOUND, userId));
        }
        BookingState bookingState = getBookingState(state);
        BookingFilter filter = getFilter(isUserOwner, userId, bookingState);
        return getBookingsByFilter(filter, SORT_BY_START_DESC);
    }

    private BookingFilter getFilter(boolean isUserOwner, long userId, BookingState bookingState) {
        final LocalDateTime currentTime = LocalDateTime.now();
        final BookingFilter bookingFilter = BookingFilter.builder()
                .owner(isUserOwner ? userId : null)
                .booker(isUserOwner ? null : userId)
                .build();
        switch (bookingState) {
            case CURRENT: {
                return bookingFilter.toBuilder()
                        .startBefore(currentTime)
                        .endAfter(currentTime)
                        .build();
            }
            case PAST: {
                return bookingFilter.toBuilder()
                        .endBefore(currentTime)
                        .build();
            }
            case FUTURE: {
                return bookingFilter.toBuilder()
                        .startAfter(currentTime)
                        .build();
            }
            case WAITING: {
                return bookingFilter.toBuilder()
                        .status(BookingStatus.WAITING)
                        .build();
            }
            case REJECTED: {
                return bookingFilter.toBuilder()
                        .status(BookingStatus.REJECTED)
                        .build();
            }
            /* for state = ALL */
            default: {
                return bookingFilter;
            }
        }
    }

    @NotNull
    private Iterable<Booking> getBookingsByFilter(BookingFilter filter, Sort order) {
        final Predicate predicate = QPredicate.builder()
                .add(filter.getBooker(), booking.booker.id::eq)
                .add(filter.getItem(), booking.item.id::eq)
                .add(filter.getOwner(), booking.item.owner.id::eq)
                .add(filter.getStartBefore(), booking.start::before)
                .add(filter.getEndBefore(), booking.end::before)
                .add(filter.getStartAfter(), booking.start::after)
                .add(filter.getEndAfter(), booking.end::after)
                .add(filter.getStatus(), booking.status::eq)
                .buildAnd();
        return repository.findAll(predicate, order);
    }

    @NotNull
    private BookingState getBookingState(String state) {
        try {
            return BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RequestException("Unknown state: " + state);
        }
    }

    private boolean isStartBeforeEnd(BookingDto bookingDto) {
        return bookingDto.getStart().isBefore(bookingDto.getEnd());
    }
}
