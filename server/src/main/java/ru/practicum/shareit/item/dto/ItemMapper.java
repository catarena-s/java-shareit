package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemMapper {

    public static ItemDtoResponse toItemDto(Item item) {
        ItemRequest request = item.getRequest();
        return ItemDtoResponse.builder()
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

    public static ItemDtoShortResponse toShort(Item item) {
        ItemRequest request = item.getRequest();
        return ItemDtoShortResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .requestId(request == null ? null : request.getId())
                .build();
    }

    public static ItemDtoResponse toItemDto(Item item, BookingDtoShort last, BookingDtoShort next, List<CommentDtoResponse> commentDto) {
        return ItemDtoResponse.builder()
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
