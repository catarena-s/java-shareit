package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        ItemRequest request = item.getRequest();
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .requestId(request == null ? null : request.getId())
                .comments(Collections.emptyList())
                .build();
    }

    public static Item mapToItem(ItemDto item, User user) {
        return Item.builder()
                .owner(user)
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    public static ItemDtoShort toShort(Item item) {
        ItemRequest request = item.getRequest();
        return ItemDtoShort.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .requestId(request == null ? null : request.getId())
                .build();
    }

    public static ItemDto toItemDto(Item item, BookingShortDto last, BookingShortDto next, List<CommentDto> commentDto) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .lastBooking(last)
                .nextBooking(next)
                .comments(commentDto)
                .build();
    }
}
