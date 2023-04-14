package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemRepository {
    /**
     * Get all items by owner
     * @param userId - owner id
     * @return collection of items
     */
    Collection<Item> getAllByOwner(long userId);

    /**
     * Get item by id
     * @param itemId - item id
     * @return item
     */
    Item getById(long itemId);

    /**
     * Create item
     * @param item - input item data
     * @param userId - owner id
     * @return new item
     */
    Item create(ItemDto item, long userId);

    /**
     * Update item's data
     * @param item new item data
     * @param itemId item's id
     * @param userId user updated item
     * @return
     */
    Item update(ItemDto item, long itemId, long userId);

    /**
     * Returns a collection of items for search substring by name or description
     * @param text search substring
     * @return collection of items
     */
    Collection<Item> search(String text);

    /**
     * Check item with itemId is existing in storage for owner with userId
     * @param itemId item's id
     * @param userId owner's id
     * @return true or false
     */
    boolean existItemsByOwner(long itemId, long userId);
}
