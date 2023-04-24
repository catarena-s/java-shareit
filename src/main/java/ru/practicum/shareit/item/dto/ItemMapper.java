package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Item;

public class ItemMapper {
    private ItemMapper() {
    }

    public static ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
           //     .request(item.getRequest() != null ? item.getRequest().getId() : null)
                .build();
    }

    public static Item mapToItem(ItemDto item, long userId) {
        return Item.builder()
                .owner(userId)
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }
}