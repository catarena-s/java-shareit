package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ItemInMemoryRepository implements ItemRepository {
    private final Map<Long, List<Item>> items = new HashMap<>();
    private long lastId = 0;

    @Override
    public Collection<Item> getAllByOwner(long userId) {
        return items.getOrDefault(userId, Collections.emptyList());
    }

    @Override
    public Item getById(long itemId) {
        return items.values().stream()
                .flatMap(Collection::stream)
                .filter(f -> f.getId() == itemId)
                .findFirst()
                .orElseThrow(() -> {
                    throw new NotFoundException(String.format("Item with id=%s not found", itemId));
                });
    }

    @Override
    public Item create(ItemDto item, long userId) {
        Item newItem = Item.builder()
                .id(getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .owner(userId)
                .build();
        List<Item> itemList = items.getOrDefault(userId, new ArrayList<>());
        itemList.add(newItem);
        items.put(userId, itemList);
        return newItem;
    }

    @Override
    public Item update(ItemDto item, long itemId, long userId) {
        List<Item> itemList = items.getOrDefault(userId, Collections.emptyList());

        Item updatedItem = itemList.stream()
                .filter(f -> f.getId() == itemId && f.getOwner() == userId)
                .findFirst()
                .get();
        if (item.getName() != null)
            updatedItem.setName(item.getName());
        if (item.getDescription() != null)
            updatedItem.setDescription(item.getDescription());
        if (item.getAvailable() != null)
            updatedItem.setAvailable(item.getAvailable());

        items.put(userId, itemList);
        return updatedItem;
    }

    @Override
    public Collection<Item> search(String text) {
        return items.values().stream()
                .flatMap(Collection::stream)
                .filter(f -> (f.isAvailable() &&
                        (f.getDescription().toLowerCase().contains(text.toLowerCase())) ||
                        f.getName().toLowerCase().contains(text.toLowerCase())))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existItemsByOwner(long itemId, long userId) {
        return items.getOrDefault(userId, Collections.emptyList())
                .stream()
                .anyMatch(f -> f.getId() == itemId && f.getOwner() == userId);
    }

    private long getId() {
        return ++lastId;
    }
}
