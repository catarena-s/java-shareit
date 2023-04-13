package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;
import java.util.List;

public interface ItemService {
    /**
     * Get all items by owner
     * @param userId owner id
     * @return collection of items, converted to DTO
     */
    Collection<ItemDto> getAllByOwner(long userId);

    /**
     * Get item by id
     * @param itemId
     * @return item, converted to DTO
     */
    ItemDto getById(long itemId);

    /**
     * Create new item
     * @param item
     * @param userId - item's owner
     * @return new item, converted to DTO
     */
    ItemDto create(ItemDto item, long userId);

    /**
     * Update item's data
     * @param item new item data
     * @param itemId item's id
     * @param userId user updated item
     * @return update item, converted to DTO
     */
    ItemDto update(ItemDto item, long itemId, long userId);

    /**
     * Returns a collection of items for search substring by name or description
     * @param userId
     * @param text - search substring
     * @return collection of items, converted to DTO
     */
    List<ItemDto> search(long userId, String text);
}
