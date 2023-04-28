package ru.practicum.shareit.booking;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapping;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.QBooking;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.RequestException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    protected static final Sort SORT_BY_START_DESC = Sort.by("start").descending();
    protected static final Sort SORT_BY_END_ASC = Sort.by("end").descending();

    private final UserService userService;
    private final BookingRepository repository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public BookingDto createBooking(long userId, BookingDto bookingDto) {
        if (!validDates(bookingDto)) {
            throw new RequestException(String.format("NotValid Dates", bookingDto.getItemId()));
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d not found", userId)));

        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException(String.format("Item with id=%d not found", bookingDto.getItemId())));

        if (item.getOwner().getId() == userId) {
            throw new NotFoundException(String.format("Item with id=%d not found", bookingDto.getItemId()));
        }

        if (!item.isAvailable()) {
            throw new RequestException(String.format("Item with id=%d not found", bookingDto.getItemId()));
        }

        Booking booking = repository.save(BookingMapping.toBooking(bookingDto, item, user));
        return BookingMapping.toDto(booking);
    }

    private boolean validDates(BookingDto bookingDto) {
        return bookingDto.getStart().isBefore(bookingDto.getEnd());
    }

    @Override
    public BookingDto approveBooking(long userId, long bookingId, boolean isApproved) {
        if (repository.existsByIdAndBookerId(bookingId, userId)) {
            throw new NotFoundException("Booker cannot change status");
        }
//        Booking booking = repository.findById(bookingId)
//        if (!repository.existsByIdAndOwnerAndStatus(bookingId, userId, BookingStatus.WAITING)) {
//            throw new RequestException("No access");
//        }

        Booking booking = repository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Booking with id=%d not found", bookingId)));

        if (!BookingStatus.WAITING.equals(booking.getStatus()) || booking.getItem().getOwner().getId() != userId) {
            throw new RequestException("No access");
        }

        booking.setStatus(isApproved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = repository.save(booking);
        return BookingMapping.toDto(updatedBooking);
    }

    @Override
    public List<BookingDto> getAllByBooker(long userId, String state) {
        if (!userService.existUser(userId)) {
            throw new NotFoundException(String.format("User with id=%d not found", userId));
        }
        BookingState bookingState = getBookingState(state);
        List<Booking> bookings = new ArrayList<>();
        final LocalDateTime currentTime = LocalDateTime.now();
        switch (bookingState) {
            case ALL: {
                bookings = repository.findAllByBookerId(userId, SORT_BY_START_DESC);
                break;
            }
            case CURRENT: {
                bookings = repository.findAllByBookerIdAndStartBeforeAndEndAfter(
                        userId, currentTime, currentTime, SORT_BY_START_DESC);
                break;
            }
            case PAST: {
                bookings = repository.findAllByBookerIdAndEndBefore(
                        userId, currentTime, SORT_BY_END_ASC);
                break;
            }
            case FUTURE: {
                bookings = repository.findAllByBookerIdAndStartAfter(
                        userId, currentTime, SORT_BY_START_DESC);
                break;
            }
            case WAITING: {
                bookings = repository.findAllByBookerIdAndStatus(
                        userId, BookingStatus.WAITING, SORT_BY_START_DESC);
                break;
            }
            case REJECTED: {
                bookings = repository.findAllByBookerIdAndStatus(
                        userId, BookingStatus.REJECTED, SORT_BY_START_DESC);
                break;
            }
        }
        return BookingMapping.toListDto(bookings);

    }

    @Override
    public BookingDto getBookingByIdForUser(long userId, long bookingId) {
        if (!userService.existUser(userId)) {
            throw new NotFoundException(String.format("User with id=%d not found", userId));
        }
        Booking booking = repository.findByIdAndBookerOrOwner(userId, bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Booking with id=%d not found", bookingId)));
        return BookingMapping.toDto(booking);
    }

    @Override
    public List<BookingDto> getByItemsOwner(long userId, String state) {
        if (!userService.existUser(userId)) {
            throw new NotFoundException(String.format("User with id=%d not found", userId));
        }
        BookingState bookingState = getBookingState(state);

        Iterable<Booking> bookings = new ArrayList<>();
        final LocalDateTime currentTime = LocalDateTime.now();
        switch (bookingState) {
            case ALL: {
                BooleanExpression ex = QBooking.booking.item.owner.id.eq(userId);
                bookings = repository.findAll(ex, SORT_BY_START_DESC);
                break;
            }
            case CURRENT: {
                BooleanExpression ex = QBooking.booking.item.owner.id.eq(userId)
                        .and(QBooking.booking.start.before(currentTime))
                        .and(QBooking.booking.end.after(currentTime));
                bookings = repository.findAll(ex, SORT_BY_START_DESC);
//                bookings = repository.findAllByOwnerAndStart(userId, currentTime);
                break;
            }
            case PAST: {
                BooleanExpression ex = QBooking.booking.item.owner.id.eq(userId)
                        .and(QBooking.booking.end.before(currentTime));
                bookings = repository.findAll(ex, SORT_BY_START_DESC);
//                bookings = repository.findAllByOwnerAndStartBefore(userId, currentTime);
                break;
            }
            case FUTURE: {
                BooleanExpression ex = QBooking.booking.item.owner.id.eq(userId)
                        .and(QBooking.booking.start.after(currentTime));
                bookings = repository.findAll(ex, SORT_BY_START_DESC);
//                bookings = repository.findAllByOwnerAndStartAfter(userId, currentTime);
                break;
            }
            case WAITING: {
                BooleanExpression ex = QBooking.booking.item.owner.id.eq(userId)
                        .and(QBooking.booking.status.eq(BookingStatus.WAITING));
                bookings = repository.findAll(ex, SORT_BY_START_DESC);
//                bookings = repository.findAllByOwnerAndStatus(userId, BookingStatus.WAITING, SORT_BY_START_DESC);
                break;
            }
            case REJECTED: {
                BooleanExpression ex = QBooking.booking.item.owner.id.eq(userId)
                        .and(QBooking.booking.status.eq(BookingStatus.REJECTED));
                bookings = repository.findAll(ex, SORT_BY_START_DESC);
//                bookings = repository.findAllByOwnerAndStatus(userId, BookingStatus.REJECTED, SORT_BY_START_DESC);
                break;
            }
        }
        return BookingMapping.toListDto(bookings);

    }

    @NotNull
    private static BookingState getBookingState(String state) {
        try {
            return BookingState.valueOf(state);
        } catch (Exception ex) {
            throw new RequestException("Unknown state: " + state);
        }
        /*
        * return BookingState.from(state)
                .orElseThrow(() -> new RequestException(String.format("Unknown state: %s", state)));*/
    }
}
