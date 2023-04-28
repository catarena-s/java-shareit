package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapping;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.RequestException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserService userService;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private static final Sort SORT_BY_START_ASC = Sort.by(Sort.Direction.ASC, "start");

    @Override
    @Transactional(readOnly = true)
    public Collection<ItemDto> getAllByOwner(long userId) {
        if (!userService.existUser(userId)) {
            throw new NotFoundException(String.format("User with id=%d not found", userId));
        }

        List<Item> items = itemRepository.findAllByOwnerId(userId);
        if (items.isEmpty()) {
            throw new NotFoundException(String.format("User with id=%d have not items", userId));
        }
        List<Booking> bookings = bookingRepository
                .findAllByOwnerAndStatus(userId, BookingStatus.APPROVED, SORT_BY_START_ASC);
        Map<Long, List<BookingDto>> b =
                bookingRepository.findAllByOwnerAndStatus(userId, BookingStatus.APPROVED, SORT_BY_START_ASC)
                        .stream()
                        .map(BookingMapping::toDto)
                        .collect(Collectors.groupingBy(BookingDto::getItemId));

        Map<Long, List<CommentDto>> commentList = commentRepository.findAllByItemIn(items)
                .stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.groupingBy(CommentDto::getItemId));

        return items.stream()
                .map(item -> ItemMapper.toItemDto(
                        item,
                        getLastBooking(b.get(item.getId())),
                        getNextBooking(b.get(item.getId())),
                        commentList.getOrDefault(item.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
//        return itemDto;
//    })
//            .

//    collect(Collectors.toList());
        //ItemMapper.toListDto(items, b, commentList);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto getById(long itemId, long userId) {
        if (!userService.existUser(userId)) {
            throw new NotFoundException(String.format("User with id=%d not found", userId));
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Item with id=%d not found", itemId)));

        List<BookingDto> booking = bookingRepository.findByItemIdAndOwner(
                        itemId, userId, BookingStatus.APPROVED, SORT_BY_START_ASC)
                .stream()
                .map(BookingMapping::toDto)
                .collect(Collectors.toList());

        BookingShortDto last = getLastBooking(booking);
        BookingShortDto next = getNextBooking(booking);
        List<CommentDto> comments = commentRepository.findByItemId(itemId)
                .stream().map(CommentMapper::toDto)
                .collect(Collectors.toList());

        return ItemMapper.toItemDto(item, last, next, comments);
    }

    @Nullable
    private static BookingShortDto getNextBooking(List<BookingDto> booking) {
        if (booking == null || booking.isEmpty()) return null;
        return booking.stream()
                .filter(b -> b.getStart().isAfter(LocalDateTime.now()))
                .findFirst()
                .map(BookingMapping::toShortDto)
                .orElse(null);
    }

    @Nullable
    private static BookingShortDto getLastBooking(List<BookingDto> booking) {
        if (booking == null || booking.isEmpty()) return null;
        return booking.stream()
                .filter(b -> !b.getStart().isAfter(LocalDateTime.now()))
                .reduce((booking1, booking2) -> booking2)
                .map(BookingMapping::toShortDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public ItemDto create(ItemDto itemDto, long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d not found", userId)));

        Item newItem = itemRepository.save(ItemMapper.mapToItem(itemDto, user));
        return ItemMapper.toItemDto(newItem);
    }

    @Override
    @Transactional
    public ItemDto update(ItemDto itemDto, long itemId, long userId) {
        if (!userService.existUser(userId)) {
            throw new NotFoundException(String.format("User with id=%d not found", userId));
        }
        Item item = itemRepository.findByIdAndOwnerId(itemId, userId)
                .orElseThrow(() -> new NotFoundException(String.format("Item with id=%d for owner id=%d not founded", itemId, userId)));

        if (itemDto.getName() != null) item.setName(itemDto.getName());
        if (itemDto.getDescription() != null) item.setDescription(itemDto.getDescription());
        if (itemDto.getAvailable() != null) item.setAvailable(itemDto.getAvailable());

        Item updatedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> search(String text) {
        List<Item> items = itemRepository.findAllByNameOrDescriptionIgnoreCase(text);
        if (items.isEmpty())
            throw new NotFoundException(String.format("For search-string '%s' items not founded", text));

        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

//    @Override
//    @Transactional(readOnly = true)
//    public boolean existItemAndAvailable(long itemId, boolean isAvailable) {
//        return itemRepository.existsByIdAndAvailable(itemId, isAvailable);
//    }

    @Override
    @Transactional
    public CommentDto createComment(long userId, long itemId, CommentDto commentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d not found", userId)));

        if (!bookingRepository.existsByIdAndBookerIdAndStartAfter(itemId, userId, LocalDateTime.now())) {
            throw new RequestException(String.format("User with id=%d not found", userId));
        }
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d not found", userId)));

        Comment comment = commentRepository.save(CommentMapper.toComment(commentDto, user, item));
        return CommentMapper.toDto(comment);
    }
}
