package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapping;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.RequestException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.shareit.util.Constants.DEFAULT_CLOCK;
import static ru.practicum.shareit.util.Constants.MSG_ITEM_WITH_ID_NOT_FOUND;
import static ru.practicum.shareit.util.Constants.MSG_USER_WITH_ID_NOT_FOUND;
import static ru.practicum.shareit.util.Constants.SORT_BY_ID_ACS;
import static ru.practicum.shareit.util.Constants.SORT_BY_START_ASC;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final UserService userService;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;
    private Clock clock = DEFAULT_CLOCK;

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    @Transactional
    public ItemDto create(ItemDto itemDto, long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(MSG_USER_WITH_ID_NOT_FOUND, userId)));

        final Item newItem = itemRepository.save(ItemMapper.mapToItem(itemDto, user));
        if (itemDto.getRequestId() != null) {
            final long requestId = itemDto.getRequestId();
            final ItemRequest request = itemRequestRepository.findById(requestId)
                    .orElseThrow(() -> new NotFoundException(String.format("Request(id=%d) not found", requestId)));
            newItem.setRequest(request);
        }
        return ItemMapper.toItemDto(newItem);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ItemDto> getAllByOwner(long userId, PageRequest page) {
        if (!userService.existUser(userId)) {
            throw new NotFoundException(String.format(MSG_USER_WITH_ID_NOT_FOUND, userId));
        }

        final List<Item> items = (page == null)
                ? itemRepository.findAllByOwnerId(userId, SORT_BY_ID_ACS)
                : itemRepository.findAllByOwnerId(userId, page).getContent();

        if (items.isEmpty()) {
            throw new NotFoundException(String.format("User with id=%d has no items", userId));
        }

        final Map<Long, List<BookingDto>> bookings =
                bookingRepository.findAllByOwnerAndStatus(userId, BookingStatus.APPROVED, SORT_BY_START_ASC)
                        .stream()
                        .map(BookingMapping::toDto)
                        .collect(Collectors.groupingBy(BookingDto::getItemId));

        final Map<Long, List<CommentDto>> commentList = commentRepository.findAllByItemIn(items)
                .stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.groupingBy(CommentDto::getItemId));

        return items.stream()
                .map(item -> ItemMapper.toItemDto(
                        item,
                        getLastBooking(bookings.get(item.getId())),
                        getNextBooking(bookings.get(item.getId())),
                        commentList.getOrDefault(item.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto getById(long itemId, long userId) {
        if (!userService.existUser(userId)) {
            throw new NotFoundException(String.format(MSG_USER_WITH_ID_NOT_FOUND, userId));
        }

        final Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format(MSG_ITEM_WITH_ID_NOT_FOUND, itemId)));

        final List<BookingDto> booking = bookingRepository.findByItemIdAndOwnerAndStatus(
                        itemId, userId, BookingStatus.APPROVED, SORT_BY_START_ASC)
                .stream().map(BookingMapping::toDto)
                .collect(Collectors.toList());


        final BookingShortDto last = getLastBooking(booking);
        final BookingShortDto next = getNextBooking(booking);

        final List<CommentDto> comments = commentRepository.findAllByItemId(itemId)
                .stream().map(CommentMapper::toDto)
                .collect(Collectors.toList());

        return ItemMapper.toItemDto(item, last, next, comments);
    }

    @Override
    @Transactional
    public ItemDto update(ItemDto itemDto, long itemId, long userId) {
        if (!userService.existUser(userId)) {
            throw new NotFoundException(String.format(MSG_USER_WITH_ID_NOT_FOUND, userId));
        }
        final Item item = itemRepository.findByIdAndOwnerId(itemId, userId).orElseThrow(
                () -> new NotFoundException(String.format("Item with id=%d for owner id=%d not found", itemId, userId)));

        if (itemDto.getName() != null) item.setName(itemDto.getName());
        if (itemDto.getDescription() != null) item.setDescription(itemDto.getDescription());
        if (itemDto.getAvailable() != null) item.setAvailable(itemDto.getAvailable());

        final Item updatedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> search(String text, PageRequest page) {
        final List<Item> items = (page == null)
                ? itemRepository.findAllByNameOrDescriptionIgnoreCase(text, SORT_BY_ID_ACS)
                : itemRepository.findAllByNameOrDescriptionIgnoreCase(text, page).getContent();

        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(long userId, long itemId, CommentDto commentDto) {
        final LocalDateTime currentTime = LocalDateTime.now(clock);
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(MSG_USER_WITH_ID_NOT_FOUND, userId)));

        if (commentRepository.existsByItemIdAndAuthorId(itemId, userId)) {
            throw new RequestException(String.format(
                    "Comment for item(id=%d) from user(id=%d) already exists", itemId, userId));
        }


        final Item item = bookingRepository.findByItemIdAndBookerIdAndEndBefore(itemId, userId, currentTime)
                .orElseThrow(() -> new RequestException(String.format(
                        "User(id=%d) has never booked the item(id=%d)", userId, itemId)));


        final Comment comment = commentRepository.save(CommentMapper.toComment(commentDto, user, item, currentTime));
        return CommentMapper.toDto(comment);
    }

    @Nullable
    private BookingShortDto getNextBooking(List<BookingDto> booking) {
        if (booking == null || booking.isEmpty()) return null;
        final LocalDateTime currentTime = LocalDateTime.now(clock);
        return booking.stream()
                .filter(b -> b.getStart().isAfter(currentTime))
                .findFirst()
                .map(BookingMapping::toShortDto)
                .orElse(null);
    }

    @Nullable
    private BookingShortDto getLastBooking(List<BookingDto> booking) {
        if (booking == null || booking.isEmpty()) return null;
        final LocalDateTime currentTime = LocalDateTime.now(clock);
        return booking.stream()
                .filter(b -> b.getStart().isBefore(currentTime))
                .reduce((booking1, booking2) -> booking2)
                .map(BookingMapping::toShortDto)
                .orElse(null);
    }
}
