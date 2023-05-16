package ru.practicum.shareit.item;

import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;
import java.util.List;

public interface ItemService {
    /**
     * Get all items by owner
     * @param userId owner id
     * @param page
     * @return collection of items, converted to DTO
     */
    Collection<ItemDto> getAllByOwner(long userId, PageRequest page);

    /**
     * Get item by id
     * @param itemId item id
     * @param userId user id
     * @return item, converted to DTO
     */
    ItemDto getById(long itemId, long userId);

    /**
     * Create new item
     * @param item - item
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
     * @param text - search substring
     * @param page
     * @return collection of items, converted to DTO
     */
    List<ItemDto> search(String text, PageRequest page);

    /**
     * Create comment for item
     * @param userId user had booked
     * @param itemId item
     * @param commentDto comment
     * @return comment, converted to DTO
     */
    CommentDto addComment(long userId, long itemId, CommentDto commentDto);
}
