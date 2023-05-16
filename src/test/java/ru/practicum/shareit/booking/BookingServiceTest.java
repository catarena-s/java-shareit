package ru.practicum.shareit.booking;

import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapping;
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

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.TestInitDataUtil.makeBooking;
import static ru.practicum.shareit.TestInitDataUtil.makeItem;
import static ru.practicum.shareit.TestInitDataUtil.makeUser;
import static ru.practicum.shareit.booking.enums.BookingStatus.APPROVED;
import static ru.practicum.shareit.booking.enums.BookingStatus.REJECTED;
import static ru.practicum.shareit.booking.enums.BookingStatus.WAITING;
import static ru.practicum.shareit.util.Constants.MSG_ITEM_WITH_ID_NOT_FOUND;
import static ru.practicum.shareit.util.Constants.MSG_USER_WITH_ID_NOT_FOUND;
import static ru.practicum.shareit.util.Constants.SORT_BY_START_DESC;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock
    private UserService userService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    private final Clock clock = Constants.TEST_CLOCK;
    @InjectMocks
    private BookingServiceImpl bookingService;
    private List<User> userList;
    private List<Item> itemList;
    private List<Booking> bookingList;
    private LocalDateTime currentTime;

    @BeforeEach
    void setUp() {
        bookingService.setClock(clock);
        userList = List.of(
                makeUser(1L, "Jon", "jon@mail.ru"),
                makeUser(2L, "Jane", "jane@mail.ru"),
                makeUser(3L, "Mary", "mary@mail.ru")
        );

        itemList = List.of(
                makeItem(1L, "item1", "item1 description", true, userList.get(0), null),
                makeItem(2L, "item2", "item2 description", true, userList.get(1), null),
                makeItem(3L, "item3", "item23 description", true, userList.get(1), null),
                makeItem(4L, "item4", "item4 description", false, userList.get(0), null)
        );

        currentTime = LocalDateTime.now(clock);
        bookingList = List.of(
                makeBooking(1L, itemList.get(0), userList.get(1), currentTime.minusDays(2), currentTime.plusDays(1), WAITING),
                makeBooking(2L, itemList.get(0), userList.get(2), currentTime.minusDays(2), currentTime.plusDays(1), WAITING),
                makeBooking(3L, itemList.get(1), userList.get(2), currentTime.minusDays(2), currentTime.plusDays(1), WAITING),
                makeBooking(4L, itemList.get(1), userList.get(0), currentTime.minusDays(2), currentTime.plusDays(1), APPROVED),
                makeBooking(5L, itemList.get(0), userList.get(2), currentTime.minusDays(2), currentTime.minusDays(1), WAITING),
                makeBooking(6L, itemList.get(1), userList.get(2), currentTime.minusDays(1), currentTime.plusDays(2), WAITING)
        );


    }

    @Test
    void createBooking_withCorrectData() {
        final User booker = userList.get(2);
        final long bookerId = booker.getId();
        final Item item = itemList.get(0);
        final long itemId = item.getId();
        final LocalDateTime start = currentTime.minusDays(2);
        final LocalDateTime end = currentTime.plusDays(1);
        final Booking newBooking = Booking.builder()
                .booker(booker)
                .item(item)
                .start(start)
                .end(end)
                .status(WAITING)
                .build();

        final BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.existsApprovedBookingForItemWithCrossTime(anyLong(), any(), any())).thenReturn(false);
        when(bookingRepository.save(any())).thenReturn(newBooking);

        final BookingDto actualBooking = bookingService.createBooking(bookerId, bookingDto);

        assertEquals(BookingMapping.toDto(newBooking), actualBooking);

        verify(userRepository, times(1)).findById(bookerId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(bookingRepository, times(1)).existsApprovedBookingForItemWithCrossTime(itemId, start, end);
        verify(bookingRepository, times(1)).save(newBooking);
    }

    @Test
    void createBooking_withNotValidDates() {
        final User booker = userList.get(0);
        final long bookerId = booker.getId();
        final Item item = itemList.get(0);
        final long itemId = item.getId();
        final LocalDateTime start = currentTime.plusDays(2);
        final LocalDateTime end = currentTime.minusDays(1);
        final Booking newBooking = Booking.builder()
                .booker(booker)
                .item(item)
                .start(start)
                .end(end)
                .status(WAITING)
                .build();

        final BookingDto dto = BookingMapping.toDto(newBooking);

        assertThrows(ValidateException.class, () -> bookingService.createBooking(bookerId, dto));

        verify(userRepository, never()).findById(bookerId);
        verify(itemRepository, never()).findById(itemId);
        verify(bookingRepository, never()).existsApprovedBookingForItemWithCrossTime(itemId, start, end);
        verify(bookingRepository, never()).save(newBooking);
    }

    @Test
    void createBooking_withNotExitedUser() {
        final long bookerId = 5L;
        final long itemId = 1L;
        final LocalDateTime start = currentTime.minusDays(2);
        final LocalDateTime end = currentTime.plusDays(1);
        final BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());


        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(bookerId, bookingDto));

        assertEquals(String.format(MSG_USER_WITH_ID_NOT_FOUND, bookerId), exception.getMessage());

        verify(userRepository, times(1)).findById(bookerId);
        verify(itemRepository, never()).findById(itemId);
        verify(bookingRepository, never()).existsApprovedBookingForItemWithCrossTime(itemId, start, end);
        verify(bookingRepository, never()).save(new Booking());
    }

    @Test
    void createBooking_withNotExitedItem() {
        final User booker = userList.get(0);
        final long bookerId = booker.getId();
        final long itemId = 10L;
        final LocalDateTime start = currentTime.minusDays(2);
        final LocalDateTime end = currentTime.plusDays(1);
        final BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(bookerId, bookingDto));

        assertEquals(String.format(MSG_ITEM_WITH_ID_NOT_FOUND, itemId), exception.getMessage());

        verify(userRepository, times(1)).findById(bookerId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(bookingRepository, never()).existsApprovedBookingForItemWithCrossTime(itemId, start, end);
        verify(bookingRepository, never()).save(new Booking());
    }

    @Test
    void createBooking_withBookerIsItemOwner() {
        final User booker = userList.get(0);
        final long bookerId = booker.getId();
        final Item item = itemList.get(0);
        final long itemId = item.getId();
        final LocalDateTime start = currentTime.minusDays(2);
        final LocalDateTime end = currentTime.plusDays(1);
        final BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));


        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(bookerId, bookingDto));

        assertEquals("The owner cannot book his item", exception.getMessage());

        verify(userRepository, times(1)).findById(bookerId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(bookingRepository, never()).existsApprovedBookingForItemWithCrossTime(itemId, start, end);
        verify(bookingRepository, never()).save(new Booking());
    }

    @Test
    void createBooking_withItemNotAvailable() {
        final User booker = userList.get(1);
        final long bookerId = booker.getId();
        final Item item = itemList.get(3);
        final long itemId = item.getId();
        final LocalDateTime start = currentTime.minusDays(2);
        final LocalDateTime end = currentTime.plusDays(1);
        final BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));


        final AvailableException exception = assertThrows(AvailableException.class,
                () -> bookingService.createBooking(bookerId, bookingDto));

        assertEquals(String.format("Item with id=%d is not available", itemId), exception.getMessage());

        verify(userRepository, times(1)).findById(bookerId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(bookingRepository, never()).existsApprovedBookingForItemWithCrossTime(itemId, start, end);
        verify(bookingRepository, never()).save(new Booking());
    }

    @Test
    void createBooking_whenCrossTime() {
        final User booker = userList.get(1);
        final long bookerId = booker.getId();
        final Item item = itemList.get(0);
        final long itemId = item.getId();
        final LocalDateTime start = currentTime.minusDays(1);
        final LocalDateTime end = currentTime.plusDays(2);
        final BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.existsApprovedBookingForItemWithCrossTime(anyLong(), any(), any())).thenReturn(true);


        final AvailableException exception = assertThrows(AvailableException.class,
                () -> bookingService.createBooking(bookerId, bookingDto));

        assertEquals(String.format(
                "Booking item(id=%d) is not available for dates from %s to %s",
                itemId, start, end), exception.getMessage());

        verify(userRepository, times(1)).findById(bookerId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(bookingRepository, times(1)).existsApprovedBookingForItemWithCrossTime(itemId, start, end);
        verify(bookingRepository, never()).save(new Booking());
    }


    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void approveBooking_whenCorrectBooking(boolean isApproved) {
        final Booking booking = bookingList.get(0);
        final long bookingId = booking.getId();
        final long userId = booking.getItem().getOwner().getId();
        final LocalDateTime start = booking.getStart();
        final LocalDateTime end = booking.getEnd();

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository
                .existsApprovedBookingForItemWithCrossTime(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        final Booking exitedBooking = booking.toBuilder()
                .status(isApproved ? APPROVED : REJECTED)
                .build();
        when(bookingRepository.save(any())).thenReturn(exitedBooking);

        final BookingDto actualBooking = bookingService.approveBooking(userId, bookingId, isApproved);
        assertEquals(BookingMapping.toDto(exitedBooking), actualBooking);

        verify(bookingRepository, times(1)).findById(bookingId);
        verify(bookingRepository, times(1)).existsApprovedBookingForItemWithCrossTime(bookingId, start, end);
        verify(bookingRepository, times(1)).save(exitedBooking);
    }

    @Test
    void approveBooking_whenUserIsBooker() {
        final Booking booking = bookingList.get(0);
        final long bookingId = booking.getId();
        final long userId = booking.getBooker().getId();
        final LocalDateTime start = booking.getStart();
        final LocalDateTime end = booking.getEnd();

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.approveBooking(userId, bookingId, true));

        assertEquals("Booker cannot change booking status", exception.getMessage());

        verify(bookingRepository, times(1)).findById(bookingId);
        verify(bookingRepository, never()).existsApprovedBookingForItemWithCrossTime(bookingId, start, end);
        verify(bookingRepository, never()).save(new Booking());
    }

    @Test
    void approveBooking_whenStatusNotWAITING() {
        final Booking booking = bookingList.get(3);
        final long bookingId = booking.getId();
        final long userId = booking.getItem().getOwner().getId();
        final LocalDateTime start = booking.getStart();
        final LocalDateTime end = booking.getEnd();

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        final RequestException exception = assertThrows(RequestException.class,
                () -> bookingService.approveBooking(userId, bookingId, true));

        assertEquals("The booking status should be 'WAITING'", exception.getMessage());

        verify(bookingRepository, times(1)).findById(bookingId);
        verify(bookingRepository, never()).existsApprovedBookingForItemWithCrossTime(bookingId, start, end);
        verify(bookingRepository, never()).save(new Booking());
    }

    @Test
    void approveBooking_whenUserNotOwner() {
        final Booking booking = bookingList.get(0);
        final long bookingId = booking.getId();
        final long userId = userList.get(2).getId();
        final LocalDateTime start = booking.getStart();
        final LocalDateTime end = booking.getEnd();

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        final NoAccessException exception = assertThrows(NoAccessException.class,
                () -> bookingService.approveBooking(userId, bookingId, true));

        assertEquals(exception.getMessage(), String.format("The user(id=%d) does not have access to approve booking.", userId));

        verify(bookingRepository, times(1)).findById(bookingId);
        verify(bookingRepository, never()).existsApprovedBookingForItemWithCrossTime(bookingId, start, end);
        verify(bookingRepository, never()).save(new Booking());
    }

    @Test
    void approveBooking_whenEndBeforeNow() {
        final Booking booking = bookingList.get(4);
        final long bookingId = booking.getId();
        final long userId = booking.getItem().getOwner().getId();
        final LocalDateTime start = booking.getStart();
        final LocalDateTime end = booking.getEnd();

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        final NoAccessException exception = assertThrows(NoAccessException.class,
                () -> bookingService.approveBooking(userId, bookingId, true));

        assertEquals("You cannot confirm a booking that has already expired", exception.getMessage());

        verify(bookingRepository, times(1)).findById(bookingId);
        verify(bookingRepository, never()).existsApprovedBookingForItemWithCrossTime(bookingId, start, end);
        verify(bookingRepository, never()).save(new Booking());
    }

    @Test
    void approveBooking_whenCrossTime() {
        final Booking booking = bookingList.get(5);
        final long bookingId = booking.getId();
        final long userId = booking.getItem().getOwner().getId();
        final long itemId = booking.getItem().getId();
        final LocalDateTime start = booking.getStart();
        final LocalDateTime end = booking.getEnd();

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.existsApprovedBookingForItemWithCrossTime(anyLong(), any(), any())).thenReturn(true);

        final AvailableException exception = assertThrows(AvailableException.class,
                () -> bookingService.approveBooking(userId, bookingId, true));

        assertEquals(exception.getMessage(),
                String.format(
                        "Booking(id=%d) for item(id=%d) is not available for approve for dates from %s to %s",
                        bookingId, itemId, start, end));

        verify(bookingRepository, times(1)).findById(bookingId);
        verify(bookingRepository, times(1)).existsApprovedBookingForItemWithCrossTime(itemId, start, end);
        verify(bookingRepository, never()).save(new Booking());
    }

    @Test
    void approveBooking_withBookingNotExist() {
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        final long bookingId = 5L;
        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.approveBooking(1L, bookingId, true));

        assertEquals(String.format(Constants.MSG_BOOKING_WITH_ID_NOT_FOUND, bookingId), exception.getMessage());

        verify(bookingRepository, times(1)).findById(bookingId);

        verify(bookingRepository, never()).existsApprovedBookingForItemWithCrossTime(bookingId, currentTime.minusDays(1), currentTime.plusDays(1));
        verify(bookingRepository, never()).save(new Booking());
    }

    @Test
    void getBookingByIdForUser() {
        final long userId = 1L;
        final long bookingID = 1L;
        final Booking exitedBooking = bookingList.get(0);
        final BookingDto exitedBookingDto = BookingMapping.toDto(exitedBooking);

        when(userService.existUser(anyLong())).thenReturn(true);
        when(bookingRepository.findByIdAndBookerOrOwner(anyLong(), anyLong()))
                .thenReturn(Optional.ofNullable(exitedBooking));

        final BookingDto actualBooking = bookingService.getBookingByIdForUser(userId, bookingID);
        assertEquals(exitedBookingDto, actualBooking);

        verify(userService, times(1)).existUser(userId);
        verify(bookingRepository, times(1)).findByIdAndBookerOrOwner(userId, bookingID);
    }

    @Test
    void getBookingByIdForUser_withNotExitUser() {
        final long userId = 1L;
        final long bookingID = 1L;

        when(userService.existUser(anyLong())).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingByIdForUser(userId, bookingID));

        verify(userService, times(1)).existUser(userId);
        verify(bookingRepository, never()).findByIdAndBookerOrOwner(userId, bookingID);
    }

    @Test
    void getBookingByIdForUser_withNotExitBooking() {
        final long userId = 1L;
        final long bookingID = 1L;

        when(userService.existUser(anyLong())).thenReturn(true);
        when(bookingRepository.findByIdAndBookerOrOwner(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingByIdForUser(userId, bookingID));

        verify(userService, times(1)).existUser(userId);
        verify(bookingRepository, times(1)).findByIdAndBookerOrOwner(userId, bookingID);
    }

    @Test
    void getAllByBooker() {
        User expectedUser = userList.get(2);
        final List<Booking> expectedList = List.of(bookingList.get(1), bookingList.get(2));
        final List<BookingDto> dto = List.of(
                BookingMapping.toDto(bookingList.get(1)),
                BookingMapping.toDto(bookingList.get(2))
        );

        when(userService.existUser(anyLong())).thenReturn(true);
        when(bookingRepository.findAll(any(Predicate.class), any(Sort.class)))
                .thenReturn(expectedList);

        final List<BookingDto> actualList = bookingService.getAllByBooker(expectedUser.getId(), "WAITING", null);

        assertEquals(dto, actualList);

    }

    @Test
    void getAllByBooker_withPagination() {
        PageRequest page = PageRequest.of(0, 1, SORT_BY_START_DESC);
        User expectedUser = userList.get(2);
        final List<Booking> expectedList = List.of(bookingList.get(1), bookingList.get(2));
        final List<BookingDto> dto = List.of(
                BookingMapping.toDto(bookingList.get(1)),
                BookingMapping.toDto(bookingList.get(2))
        );

        when(userService.existUser(anyLong())).thenReturn(true);
        when(bookingRepository.findAll(any(Predicate.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(expectedList));

        final List<BookingDto> actualList = bookingService.getAllByBooker(expectedUser.getId(), "WAITING", page);

        assertEquals(dto, actualList);

    }

    @Test
    void getAllByBooker_NotExistUser() {
        when(userService.existUser(anyLong())).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> bookingService.getAllByBooker(5L, "WAITING", null));
    }

    @Test
    void getAllByBooker_withWrongStatus() {
        final String state = "UNKNOWN_STATUS";

        when(userService.existUser(anyLong())).thenReturn(true);

        final RequestException exception = assertThrows(RequestException.class,
                () -> bookingService.getAllByBooker(5L, state, null));

        assertEquals("Unknown state: " + state, exception.getMessage());
    }

    @Test
    void getAllByOwner() {
        User expectedUser = userList.get(0);
        final List<Booking> expectedList = List.of(bookingList.get(0), bookingList.get(1));
        final List<BookingDto> dto = List.of(
                BookingMapping.toDto(bookingList.get(0)),
                BookingMapping.toDto(bookingList.get(1))
        );

        when(userService.existUser(anyLong())).thenReturn(true);
        when(bookingRepository.findAll(any(Predicate.class), any(Sort.class)))
                .thenReturn(expectedList);

        final List<BookingDto> actualList = bookingService.getAllByOwner(expectedUser.getId(), "WAITING", null);

        assertEquals(dto, actualList);
    }

    @Test
    void getAllByOwner_NotExistUser() {
        when(userService.existUser(anyLong())).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> bookingService.getAllByOwner(5L, "WAITING", null));

    }

    @Test
    void getAllByOwner_withWrongStatus() {
        final String state = "UNKNOWN_STATUS";

        when(userService.existUser(anyLong())).thenReturn(true);

        final RequestException exception = assertThrows(RequestException.class,
                () -> bookingService.getAllByOwner(5L, state, null));

        assertEquals("Unknown state: " + state, exception.getMessage());
    }

}