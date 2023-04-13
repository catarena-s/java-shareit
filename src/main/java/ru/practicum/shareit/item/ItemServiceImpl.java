package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserService userService;
    private final ItemRepository storage;

    @Override
    public Collection<ItemDto> getAllByOwner(long userId) {
        return storage.getAllByOwner(userId)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getById(long itemId) {
        Item item = storage.getById(itemId);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto create(ItemDto item, long userId) {
        if (!userService.existUser(userId)) {
            throw new NotFoundException(String.format("User with id=%d not found", userId));
        }
        Item newItem = storage.create(item, userId);
        return ItemMapper.toItemDto(newItem);
    }

    @Override
    public ItemDto update(ItemDto item, long itemId, long userId) {
        if (!userService.existUser(userId)) {
            throw new NotFoundException(String.format("User with id=%d not found", userId));
        }
        if (!storage.existItemsByOwner(itemId, userId)) {
            throw new NotFoundException(
                    String.format("Item with id=%d for owner id=%d not founded", itemId, userId));
        }
        Item updatedItem = storage.update(item, itemId, userId);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public List<ItemDto> search(long userId, String text) {
        return storage.search(userId, text)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
