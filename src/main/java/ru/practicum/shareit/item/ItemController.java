package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@Slf4j
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService service;

    @GetMapping
    public Collection<ItemDto> getAll(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.debug("Request received GET '/items'  for userId = {}", userId);
        return service.getAllByOwner(userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@PathVariable(name = "itemId") long itemId) {
        log.debug("Request received GET '/items/{}'", itemId);
        return service.getById(itemId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(
            @RequestHeader(value = "X-Sharer-User-Id", required = false) long userId,
            @Valid @RequestBody ItemDto item) {
        log.debug("Request received POST '/items' for userId = {} : {}", userId, item);
        return service.create(item, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(value = "X-Sharer-User-Id", required = false) long userId,
                          @PathVariable(name = "itemId") long itemId,
                          @RequestBody ItemDto item) {
        log.debug("Request received PATCH '/items' for userId = {} itemId = {}", userId, itemId);
        return service.update(item, itemId, userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(
            @RequestHeader(value = "X-Sharer-User-Id", required = false) long userId,
            @RequestParam(name = "text") String text) {
        log.debug("Request received GET 'GET /items/search?text={}'", text);
        log.debug("X-Sharer-User-Id={}'", userId);
        if (text.isBlank()) {
            log.debug("Parameter 'text' is empty");
            return Collections.emptyList();
        }
        return service.search(userId, text);
    }
}
