package ru.practicum.shareit.booking;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.dto.BookingMapping;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingFilter;
import ru.practicum.shareit.exceptions.AvailableException;
import ru.practicum.shareit.exceptions.NoAccessException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.RequestException;
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
    public BookingDtoResponse bookItem(long userId, BookingDto bookingDto) {
        final User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException(String.format(MSG_USER_WITH_ID_NOT_FOUND, userId)));

        final Item item = itemRepository.findById(bookingDto.getItemId()).orElseThrow(
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

        final Booking booking = repository.save(BookingMapping.toBooking(bookingDto, item, user));
        return BookingMapping.toDto(booking);
    }

    @Override
    @Transactional
    public BookingDtoResponse approveBooking(long userId, long bookingId, boolean isApproved) {
        final Booking booking = repository.findById(bookingId).orElseThrow(
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
        if (isEndDateInPast(booking)) {
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
    public BookingDtoResponse getBookingByIdForUser(long userId, long bookingId) {
        if (!userService.existUser(userId)) {
            throw new NotFoundException(String.format(MSG_USER_WITH_ID_NOT_FOUND, userId));
        }
        final Booking booking = repository.findByIdAndBookerOrOwner(userId, bookingId).orElseThrow(
                () -> new NotFoundException(String.format(Constants.MSG_BOOKING_WITH_ID_NOT_FOUND, bookingId)));
        return BookingMapping.toDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDtoResponse> getAllByBooker(long userId, String state, PageRequest page) {
        final Iterable<Booking> bookings = getBookingsByUserAndState(userId, state, false, page);
        return BookingMapping.toListDto(bookings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDtoResponse> getAllByOwner(long userId, String state, PageRequest page) {
        final Iterable<Booking> bookings = getBookingsByUserAndState(userId, state, true, page);
        return BookingMapping.toListDto(bookings);
    }

    @NotNull
    private Iterable<Booking> getBookingsByUserAndState(long userId, String state, boolean isUserOwner, PageRequest page) {
        if (!userService.existUser(userId)) {
            throw new NotFoundException(String.format(MSG_USER_WITH_ID_NOT_FOUND, userId));
        }
        final BookingState bookingState = getBookingState(state);
        final BookingFilter mainFilter = BookingFilter.builder()
                .owner(isUserOwner ? userId : null)
                .booker(isUserOwner ? null : userId)
                .build();
        final BookingFilter filterByState = getFilter(mainFilter, bookingState);
        return getBookingsByFilter(filterByState, page);
    }

    private BookingFilter getFilter(BookingFilter mainFilter, BookingState bookingState) {
        return bookingState.getStateFilter().getFilter(mainFilter);
    }

    @NotNull
    private Iterable<Booking> getBookingsByFilter(BookingFilter filter, PageRequest page) {
        final Predicate predicate = getPredicate(filter);
        return (page != null)
                ? repository.findAll(predicate, page.withSort(SORT_BY_START_DESC))
                : repository.findAll(predicate, SORT_BY_START_DESC);
    }

    private Predicate getPredicate(BookingFilter filter) {
        return QPredicate.builder()
                .add(filter.getBooker(), booking.booker.id::eq)
                .add(filter.getItem(), booking.item.id::eq)
                .add(filter.getOwner(), booking.item.owner.id::eq)
                .add(filter.getStartBefore(), booking.start::before)
                .add(filter.getEndBefore(), booking.end::before)
                .add(filter.getStartAfter(), booking.start::after)
                .add(filter.getEndAfter(), booking.end::after)
                .add(filter.getStatus(), booking.status::eq)
                .buildAnd();
    }

    private BookingState getBookingState(String stateParam) {
        return BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
    }

    private boolean isEndDateInPast(Booking booking) {
        return booking.getEnd().isBefore(LocalDateTime.now());
    }
}
