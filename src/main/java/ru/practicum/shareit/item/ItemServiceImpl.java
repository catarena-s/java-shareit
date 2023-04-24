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
        return storage.findAllByOwner(userId)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getById(long itemId) {
        if (!storage.existsById(itemId)) {
            throw new NotFoundException(String.format("Item with id=%d not found", itemId));
        }
        Item item = storage.getReferenceById(itemId);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto create(ItemDto item, long userId) {
        if (!userService.existUser(userId)) {
            throw new NotFoundException(String.format("User with id=%d not found", userId));
        }
        Item newItem = storage.save(ItemMapper.mapToItem(item, userId));
        return ItemMapper.toItemDto(newItem);
    }

    @Override
    public ItemDto update(ItemDto itemDto, long itemId, long userId) {
        if (!userService.existUser(userId)) {
            throw new NotFoundException(String.format("User with id=%d not found", userId));
        }
        if (!storage.existsByIdAndOwner(itemId, userId)) {
            throw new NotFoundException(
                    String.format("Item with id=%d for owner id=%d not founded", itemId, userId));
        }
        Item item = storage.getReferenceById(itemId);
        if (itemDto.getName() != null)
            item.setName(itemDto.getName());
        if (itemDto.getDescription() != null)
            item.setDescription(itemDto.getDescription());
        if (itemDto.getAvailable() != null)
            item.setAvailable(itemDto.getAvailable());

        Item updatedItem = storage.save(item);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public List<ItemDto> search(String text) {
        return storage.findAllByNameOrDescriptionIgnoreCase(text.toUpperCase())
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
