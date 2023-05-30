package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.dto.BookingMapping;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.RequestException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.TestInitDataUtil.makeBooking;
import static ru.practicum.shareit.TestInitDataUtil.makeItem;
import static ru.practicum.shareit.TestInitDataUtil.makeUser;
import static ru.practicum.shareit.booking.enums.BookingStatus.APPROVED;
import static ru.practicum.shareit.booking.enums.BookingStatus.WAITING;
import static ru.practicum.shareit.util.Constants.MSG_ITEM_WITH_ID_NOT_FOUND;
import static ru.practicum.shareit.util.Constants.MSG_USER_WITH_ID_NOT_FOUND;
import static ru.practicum.shareit.util.Constants.SORT_BY_ID_ACS;
import static ru.practicum.shareit.util.Constants.SORT_BY_START_ASC;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private ItemServiceImpl itemService;

    private List<User> userList;
    private List<Item> itemList;
    //    private final Clock clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"));
    private ItemRequest itemRequest;
    private List<Booking> bookingList;

    @BeforeEach
    void setUp() {
//        itemService.setClock(clock);
        userList = List.of(
                makeUser(1L, "Jon", "jon@mail.ru"),
                makeUser(2L, "Jane", "jane@mail.ru"),
                makeUser(3L, "Mary", "mary@mail.ru")
        );

        itemList = List.of(
                makeItem(1L, "item1", "item1 description", true, userList.get(0), null),
                makeItem(2L, "item2", "item2 description", true, userList.get(1), null),
                makeItem(3L, "item3", "item23 description", true, userList.get(1), null)
        );

        final LocalDateTime currentTime = LocalDateTime.now();

        itemRequest = ItemRequest.builder()
                .id(1L)
                .description("Item request")
                .requester(userList.get(2))
                .created(currentTime)
                .build();

        bookingList = List.of(
                makeBooking(1L, itemList.get(0), userList.get(1), currentTime.minusDays(2), currentTime.plusDays(1), WAITING),
                makeBooking(2L, itemList.get(0), userList.get(2), currentTime.plusDays(1), currentTime.plusDays(3), WAITING),
                makeBooking(3L, itemList.get(1), userList.get(2), currentTime.minusDays(2), currentTime.plusDays(1), WAITING),
                makeBooking(4L, itemList.get(1), userList.get(0), currentTime.minusDays(2), currentTime.plusDays(1), APPROVED),
                makeBooking(5L, itemList.get(0), userList.get(2), currentTime.minusDays(2), currentTime.minusDays(1), WAITING),
                makeBooking(6L, itemList.get(1), userList.get(2), currentTime.plusDays(2), currentTime.plusDays(4), WAITING)
        );
    }

    @Test
    void getById_withoutBookingAndComments() {
        Item expectedItem = itemList.get(0);
        ItemDtoResponse expectedItemDto = ItemMapper.toItemDto(expectedItem);
        expectedItemDto.setComments(Collections.emptyList());

        User expectedUser = userList.get(0);
        final long expectedItemId = expectedItem.getId();
        final long expectedUserId = expectedUser.getId();

        when(userService.existUser(anyLong())).thenReturn(true);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(expectedItem));


        final ItemDtoResponse actualItem = itemService.getById(expectedItemId, expectedUserId);

        assertEquals(expectedItemDto, actualItem);
        verify(userService, times(1)).existUser(expectedItemId);
        verify(itemRepository, times(1)).findById(expectedItemId);
        verify(bookingRepository, times(1)).findByItemIdAndOwnerAndStatus(expectedItemId, expectedUserId, BookingStatus.APPROVED, SORT_BY_START_ASC);
        verify(commentRepository, times(1)).findAllByItemId(expectedItemId);
    }

    @Test
    void getById_withBooking() {
        Item expectedItem = itemList.get(0);


        User expectedUser = userList.get(0);
        final long expectedItemId = expectedItem.getId();
        final long expectedUserId = expectedUser.getId();
        final List<Booking> bookings = List.of(bookingList.get(0), bookingList.get(1), bookingList.get(1));
        final List<BookingDtoResponse> bookingsDto = BookingMapping.toListDto(bookings);

        ItemDtoResponse expectedItemDto = ItemMapper.toItemDto(
                expectedItem,
                BookingMapping.toShortDto(bookingsDto.get(0)),
                BookingMapping.toShortDto(bookingsDto.get(1)),
                Collections.emptyList()
        );
        when(userService.existUser(anyLong())).thenReturn(true);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(expectedItem));
        when(bookingRepository.findByItemIdAndOwnerAndStatus(anyLong(), anyLong(), any(BookingStatus.class), any(Sort.class)))
                .thenReturn(bookings);

        final ItemDtoResponse actualItem = itemService.getById(expectedItemId, expectedUserId);

        assertEquals(expectedItemDto, actualItem);

        verify(userService, times(1)).existUser(expectedItemId);
        verify(itemRepository, times(1)).findById(expectedItemId);
        verify(bookingRepository, times(1)).findByItemIdAndOwnerAndStatus(expectedItemId, expectedUserId, BookingStatus.APPROVED, SORT_BY_START_ASC);
        verify(commentRepository, times(1)).findAllByItemId(expectedItemId);
    }

    @Test
    void getById_withoutBookingAndComments_notExistUser() {
        final Item expectedItem = itemList.get(0);
        final long expectedItemId = expectedItem.getId();
        final long expectedOwnerId = expectedItem.getOwner().getId();

        when(userService.existUser(anyLong())).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> itemService.getById(expectedItemId, 5L));

        verify(userService, times(1)).existUser(5L);
        verify(itemRepository, never()).findById(expectedItemId);
        verify(bookingRepository, never()).findByItemIdAndOwnerAndStatus(expectedItemId, expectedOwnerId, BookingStatus.APPROVED, SORT_BY_START_ASC);
        verify(commentRepository, never()).findAllByItemId(expectedItemId);
    }

    @Test
    void getById_withoutBookingAndComments_notExistItem() {
        final Item expectedItem = itemList.get(0);
        final long expectedItemId = expectedItem.getId();
        final long expectedOwnerId = expectedItem.getOwner().getId();

        when(userService.existUser(anyLong())).thenReturn(true);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.getById(expectedItemId, expectedOwnerId));

        assertEquals(String.format(MSG_ITEM_WITH_ID_NOT_FOUND, expectedItemId), exception.getMessage());

        verify(userService, times(1)).existUser(expectedOwnerId);
        verify(itemRepository, times(1)).findById(expectedItemId);
        verify(bookingRepository, never()).findByItemIdAndOwnerAndStatus(expectedItemId, expectedOwnerId, BookingStatus.APPROVED, SORT_BY_START_ASC);
        verify(commentRepository, never()).findAllByItemId(expectedItemId);
    }

    @Test
    void create_withoutRequest() {
        final User expectedUser = userList.get(1);
        final long expectedUserId = expectedUser.getId();
        final ItemDto itemDto = ItemDto.builder()
                .name("Item Name")
                .description("description")
                .available(true)
                .build();
        final ItemDtoResponse expectedItemDto = ItemDtoResponse.builder()
                .name("Item Name")
                .description("description")
                .comments(Collections.emptyList())
                .available(true)
                .build();
        final Item expectedItem = ItemMapper.mapToItem(itemDto, expectedUser);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(expectedUser));
        when(itemRepository.save(any())).thenReturn(expectedItem);

        final ItemDtoResponse actualItemDto = itemService.create(itemDto, expectedUserId);
        assertEquals(expectedItemDto, actualItemDto);

        verify(userRepository, times(1)).findById(expectedUserId);
        verify(itemRepository, times(1)).save(expectedItem);
        verify(itemRequestRepository, never()).findById(1L);
    }

    @Test
    void create_withRequest() {
        final User expectedUser = userList.get(0);
        final long expectedUserId = expectedUser.getId();
        final long requestId = itemRequest.getId();
        final ItemDto itemDto = ItemDto.builder()
                .name("Item Name")
                .description("Item description")
                .available(true)
                .requestId(requestId)
                .build();
        final Item newItem = ItemMapper.mapToItem(itemDto, expectedUser);


        final ItemDtoResponse newItemDto = ItemDtoResponse.builder()
                .name("Item Name")
                .description("Item description")
                .available(true)
                .requestId(requestId)
                .comments(Collections.emptyList())
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(expectedUser));
        when(itemRepository.save(any())).thenReturn(newItem);
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));

        final ItemDtoResponse actualItemDto = itemService.create(itemDto, expectedUserId);
        assertEquals(newItemDto, actualItemDto);
    }

    @Test
    void create_withRequest_whenRequestNotExist() {
        final User expectedUser = userList.get(0);
        final long expectedUserId = expectedUser.getId();
        final long requestId = 5L;
        final ItemDto itemDto = ItemDto.builder()
                .name("Item Name")
                .description("Item description")
                .available(true)
                .requestId(requestId)
                .build();

        final Item newItem = Item.builder()
                .name("Item name")
                .description("Item description")
                .available(true)
                .owner(expectedUser)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(expectedUser));
        when(itemRepository.save(any())).thenReturn(newItem);
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.create(itemDto, expectedUserId));
        assertEquals(String.format("Request(id=%d) not found", requestId), exception.getMessage());

        verify(userRepository, times(1)).findById(expectedUserId);
        verify(itemRequestRepository, times(1)).findById(requestId);
    }

    @Test
    void create_NotExistUser() {
        final ItemDto expectedItemDto = ItemDto.builder()
                .name("Item Name")
                .description("description")
                .available(true)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        final long userId = 5L;
        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.create(expectedItemDto, userId));

        assertEquals(String.format(MSG_USER_WITH_ID_NOT_FOUND, userId), exception.getMessage());

        verify(userRepository, times(1)).findById(userId);
        verify(itemRepository, never()).save(new Item());
        verify(itemRepository, never()).findById(1L);
    }

    @Test
    void getAllByOwner() {
        final User expectedUser = userList.get(1);
        final long expectedOwnerId = expectedUser.getId();
        final List<Item> expectedItemList = List.of(itemList.get(1), itemList.get(2));
        final List<ItemDtoResponse> expectedItemDtoList = expectedItemList.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());


        when(userService.existUser(anyLong())).thenReturn(true);
        when(itemRepository.findAllByOwnerId(anyLong(), any(Sort.class))).thenReturn(expectedItemList);
        when(bookingRepository.findAllByOwnerAndStatus(anyLong(), any(BookingStatus.class), any(Sort.class)))
                .thenReturn(Collections.emptyList());
        when(commentRepository.findAllByItemIn(any())).thenReturn(Collections.emptyList());

        final Collection<ItemDtoResponse> actualItemListByOwner = itemService.getAllByOwner(expectedOwnerId, null);

        assertEquals(expectedItemDtoList, actualItemListByOwner);

        verify(userService, times(1)).existUser(expectedOwnerId);
        verify(itemRepository, times(1)).findAllByOwnerId(expectedOwnerId, SORT_BY_ID_ACS);
        verify(bookingRepository, times(1)).findAllByOwnerAndStatus(expectedOwnerId, BookingStatus.APPROVED, SORT_BY_START_ASC);
        verify(commentRepository, times(1)).findAllByItemIn(expectedItemList);
    }

    @Test
    void getAllByOwner_withPagination() {
        PageRequest page = PageRequest.of(0, 1, SORT_BY_ID_ACS);
        final User expectedUser = userList.get(1);
        final long expectedOwnerId = expectedUser.getId();
        final List<Item> expectedItemList = List.of(itemList.get(1), itemList.get(2));
        final List<ItemDtoResponse> expectedItemDtoList = expectedItemList.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());


        when(userService.existUser(anyLong())).thenReturn(true);
        when(itemRepository.findAllByOwnerId(anyLong(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(expectedItemList));
        when(bookingRepository.findAllByOwnerAndStatus(anyLong(), any(BookingStatus.class), any(Sort.class)))
                .thenReturn(Collections.emptyList());
        when(commentRepository.findAllByItemIn(any())).thenReturn(Collections.emptyList());

        final Collection<ItemDtoResponse> actualItemListByOwner = itemService.getAllByOwner(expectedOwnerId, page);

        assertEquals(expectedItemDtoList, actualItemListByOwner);

        verify(userService, times(1)).existUser(expectedOwnerId);
        verify(itemRepository, times(1)).findAllByOwnerId(expectedOwnerId, page);
        verify(bookingRepository, times(1)).findAllByOwnerAndStatus(expectedOwnerId, BookingStatus.APPROVED, SORT_BY_START_ASC);
        verify(commentRepository, times(1)).findAllByItemIn(expectedItemList);
    }

    @Test
    void getAllByOwner_emptyList() {
        final User expectedUser = userList.get(1);
        final long expectedOwnerId = expectedUser.getId();

        when(userService.existUser(anyLong())).thenReturn(true);
        when(itemRepository.findAllByOwnerId(anyLong(), any(Sort.class))).thenReturn(Collections.emptyList());

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.getAllByOwner(expectedOwnerId, null));

        assertEquals(String.format("User with id=%d has no items", expectedOwnerId), exception.getMessage());

        verify(userService, times(1)).existUser(expectedOwnerId);
        verify(itemRepository, times(1)).findAllByOwnerId(expectedOwnerId, SORT_BY_ID_ACS);
        verify(bookingRepository, never()).findAllByOwnerAndStatus(expectedOwnerId, BookingStatus.APPROVED, SORT_BY_START_ASC);
        verify(commentRepository, never()).findAllByItemIn(Collections.emptyList());
    }

    @Test
    void getAllByOwner_whenNotExistUser_throwException() {
        when(userService.existUser(anyLong())).thenReturn(false);

        final long ownerId = 5;
        assertThrows(NotFoundException.class,
                () -> itemService.getAllByOwner(5, null));

        verify(userService, times(1)).existUser(5);
        verify(itemRepository, never()).findAllByOwnerId(5L, SORT_BY_ID_ACS);
        verify(bookingRepository, never()).findAllByOwnerAndStatus(ownerId, BookingStatus.APPROVED, SORT_BY_START_ASC);
        verify(commentRepository, never()).findAllByItemIn(Collections.emptyList());
    }

    @ParameterizedTest
    @CsvSource({
            "Item Name, description, true",
            "Item Name, description, ",
            "Item Name, , false",
            ", description, true"
    })
    void update(String name, String description, Boolean isAvailable) {
        final ItemDto itemDto = ItemDto.builder()
                .name("Item Name")
                .description("description")
                .available(true)
                .build();

        final ItemDtoResponse expectedItemDto = ItemDtoResponse.builder()
                .name(name)
                .description(description)
                .comments(Collections.emptyList())
                .available(isAvailable)
                .build();
        final Item item = itemList.get(0);
        Item expectedItem = itemList.get(0);
        if (expectedItemDto.getAvailable() != null) {
            expectedItem.setAvailable(expectedItemDto.getAvailable());
        }
        if (expectedItemDto.getDescription() != null) {
            expectedItem.setDescription(expectedItemDto.getDescription());
        }
        if (expectedItemDto.getName() != null) {
            expectedItem.setName(expectedItemDto.getName());
        }

        when(userService.existUser(anyLong())).thenReturn(true);
        when(itemRepository.findByIdAndOwnerId(anyLong(), anyLong())).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenReturn(expectedItem);

        final long userId = item.getOwner().getId();
        final ItemDtoResponse actualItem = itemService.update(itemDto, item.getId(), userId);

        assertEquals(ItemMapper.toItemDto(expectedItem), actualItem);

        verify(userService, times(1)).existUser(userId);
        verify(itemRepository, times(1)).findByIdAndOwnerId(item.getId(), userId);
        verify(itemRepository, times(1)).save(expectedItem);
    }

    @Test
    void update_WrongOwnerId() {
        final ItemDto expectedItemDto = ItemDto.builder()
                .name("Item Name")
                .description("description")
                .available(true)
                .build();

        when(userService.existUser(anyLong())).thenReturn(true);
        when(itemRepository.findByIdAndOwnerId(anyLong(), anyLong())).thenReturn(Optional.empty());

        final long userId = 2L;
        final long itemId = 1L;
        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.update(expectedItemDto, itemId, userId));
        assertEquals(
                String.format("Item with id=%d for owner id=%d not found", itemId, userId),
                exception.getMessage());

        verify(userService, times(1)).existUser(userId);
        verify(itemRepository, times(1)).findByIdAndOwnerId(itemId, userId);
        verify(itemRepository, never()).save(new Item());
    }

    @Test
    void update_whenUserNotExist_throwException() {
        final ItemDto itemDto = ItemDto.builder()
                .name("Item Name")
                .description("description")
                .available(true)
                .build();

        when(userService.existUser(anyLong())).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> itemService.update(itemDto, 1L, 5L));

        verify(userService, times(1)).existUser(5);
        verify(itemRepository, never()).findByIdAndOwnerId(1L, 5L);
        verify(itemRepository, never()).save(new Item());
    }

    @Test
    void search() {
        String text = "item2";
        when(itemRepository.findAllByNameOrDescriptionIgnoreCase(text, SORT_BY_ID_ACS))
                .thenReturn(List.of(itemList.get(1), itemList.get(2)));

        final List<ItemDtoResponse> foundedItems = itemService.search(text, null);

        assertFalse(foundedItems.isEmpty());
        assertEquals(2, foundedItems.size());
        assertEquals(ItemMapper.toItemDto(itemList.get(1)), foundedItems.get(0));
        assertEquals(ItemMapper.toItemDto(itemList.get(2)), foundedItems.get(1));

        verify(itemRepository, times(1)).findAllByNameOrDescriptionIgnoreCase(text, SORT_BY_ID_ACS);
    }

    @Test
    void search_Pagination() {
        String text = "item2";
        PageRequest page = PageRequest.of(0, 1, SORT_BY_ID_ACS);
        when(itemRepository.findAllByNameOrDescriptionIgnoreCase(anyString(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(itemList.get(1))));

        final List<ItemDtoResponse> foundedItems = itemService.search(text, page);

        assertFalse(foundedItems.isEmpty());
        assertEquals(1, foundedItems.size());
        assertEquals(ItemMapper.toItemDto(itemList.get(1)), foundedItems.get(0));

        verify(itemRepository, times(1)).findAllByNameOrDescriptionIgnoreCase(text, page);
    }

    @Test
    void search_Pagination_EmptyList() {
        String text = "item2";
        PageRequest page = PageRequest.of(1, 3, SORT_BY_ID_ACS);
        when(itemRepository.findAllByNameOrDescriptionIgnoreCase(anyString(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        final List<ItemDtoResponse> foundedItems = itemService.search(text, page);

        assertTrue(foundedItems.isEmpty());
        verify(itemRepository, times(1)).findAllByNameOrDescriptionIgnoreCase(text, page);
    }

    @Test
    void addComment() {
        final User expectedUser = userList.get(0);
        final long expectedUserId = expectedUser.getId();
        final Item expectedItem = itemList.get(0);
        final long expectedItemId = expectedItem.getId();
        final LocalDateTime nowTime = LocalDateTime.now();
        final CommentDto commentDto = CommentDto.builder()
                .text("Comment for item1")
                .build();
        final Comment expectedComment = Comment.builder()
                .item(expectedItem)
                .text("Comment for item1")
                .author(expectedUser)
                .created(nowTime)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(expectedUser));
        when(commentRepository.existsByItemIdAndAuthorId(anyLong(), anyLong()))
                .thenReturn(false);
        when(bookingRepository.findByItemIdAndBookerIdAndEndBefore(anyLong(), anyLong(), any()))
                .thenReturn(Optional.of(expectedItem));

        when(commentRepository.save(any())).thenReturn(expectedComment);

        final CommentDtoResponse actualComment = itemService.addComment(expectedUserId, expectedItemId, commentDto);

        assertEquals(CommentMapper.toDto(expectedComment), actualComment);
    }

    @Test
    void addComment_notExistUser_throwException() {
        final LocalDateTime nowTime = LocalDateTime.now();
        final CommentDto commentDto = CommentDto.builder()
                .text("Comment for item1")
                .build();
        final long userId = 5L;
        final long itemId = 1L;

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());


        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.addComment(userId, itemId, commentDto));

        assertEquals(String.format(MSG_USER_WITH_ID_NOT_FOUND, userId), exception.getMessage());

        verify(userRepository, times(1)).findById(userId);
        verify(commentRepository, never()).existsByItemIdAndAuthorId(itemId, userId);
        verify(bookingRepository, never())
                .findByItemIdAndBookerIdAndEndBefore(itemId, userId, nowTime);
        verify(commentRepository, never()).save(new Comment());
    }

    @Test
    void addComment_commentAlreadyExist_throwException() {
        final User expectedUser = userList.get(0);
        final long expectedUserId = expectedUser.getId();
        final Item expectedItem = itemList.get(0);
        final long expectedItemId = expectedItem.getId();
        final LocalDateTime nowTime = LocalDateTime.now();
        final CommentDto commentDto = CommentDto.builder()
                .text("Comment for item1")
                .build();
        final Comment expectedComment = Comment.builder()
                .item(expectedItem)
                .text("Comment for item1")
                .author(expectedUser)
                .created(nowTime)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(expectedUser));
        when(commentRepository.existsByItemIdAndAuthorId(anyLong(), anyLong()))
                .thenReturn(true);

        final RequestException exception = assertThrows(RequestException.class,
                () -> itemService.addComment(expectedUserId, expectedItemId, commentDto));
        assertEquals(String.format(
                        "Comment for item(id=%d) from user(id=%d) already exists", expectedItemId, expectedUserId),
                exception.getMessage());

        verify(userRepository, times(1)).findById(expectedUserId);
        verify(commentRepository, times(1)).existsByItemIdAndAuthorId(expectedItemId, expectedUserId);
        verify(bookingRepository, never()).findByItemIdAndBookerIdAndEndBefore(
                expectedItemId,
                expectedUserId,
                nowTime
        );
        verify(commentRepository, never()).save(expectedComment);
    }

    @Test
    void addComment_whenUserNeverBookItem_throwException() {
        LocalDateTime currentTime = LocalDateTime.now();

        final User expectedUser = userList.get(0);
        final long expectedUserId = expectedUser.getId();
        final Item expectedItem = itemList.get(0);
        final long expectedItemId = expectedItem.getId();

        final CommentDto commentDto = CommentDto.builder()
                .text("Comment for item1")
                .build();
        final Comment expectedComment = Comment.builder()
                .item(expectedItem)
                .text("Comment for item1")
                .author(expectedUser)
                .created(currentTime)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(expectedUser));
        when(commentRepository.existsByItemIdAndAuthorId(anyLong(), anyLong()))
                .thenReturn(false);
        when(bookingRepository.findByItemIdAndBookerIdAndEndBefore(anyLong(), anyLong(), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        final RequestException exception = assertThrows(RequestException.class,
                () -> itemService.addComment(expectedUserId, expectedItemId, commentDto));

        assertEquals(String.format(
                        "User(id=%d) has never booked the item(id=%d)", expectedUserId, expectedItemId),
                exception.getMessage());

        verify(userRepository, times(1)).findById(expectedUserId);
        verify(commentRepository, times(1))
                .existsByItemIdAndAuthorId(expectedItemId, expectedUserId);
//        verify(bookingRepository, times(1))
//                .findByItemIdAndBookerIdAndEndBefore(expectedItemId, expectedUserId, currentTime);
        verify(commentRepository, never()).save(expectedComment);
    }
}