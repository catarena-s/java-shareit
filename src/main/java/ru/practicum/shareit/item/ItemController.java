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
import ru.practicum.shareit.item.dto.CommentDto;
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
        log.debug("Request received GET '/items'");
        log.debug("X-Sharer-User-Id={}", userId);
        return service.getAllByOwner(userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable(name = "itemId") long itemId) {
        log.debug("Request received GET '/items/{}'", itemId);
        log.debug("X-Sharer-User-Id={}", userId);
        return service.getById(itemId, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(
            @RequestHeader(value = "X-Sharer-User-Id", required = false) long userId,
            @Valid @RequestBody ItemDto itemDto) {
        log.debug("Request received POST '/items' : {}", itemDto);
        log.debug("X-Sharer-User-Id={}", userId);
        return service.create(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(value = "X-Sharer-User-Id", required = false) long userId,
                          @PathVariable(name = "itemId") long itemId,
                          @RequestBody ItemDto itemDto) {
        log.debug("Request received PATCH '/items/{}' : {}", itemId, itemDto);
        log.debug("X-Sharer-User-Id={}", userId);
        return service.update(itemDto, itemId, userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(
            @RequestHeader(value = "X-Sharer-User-Id", required = false) long userId,
            @RequestParam(name = "text") String text) {
        log.debug("Request received GET '/items/search?text={}'", text);
        log.debug("X-Sharer-User-Id={}", userId);
        if (text.isBlank()) {
            log.debug("Parameter 'text' is empty");
            return Collections.emptyList();
        }
        return service.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createCommentToItem(@RequestHeader(value = "X-Sharer-User-Id", required = false) long userId,
                                          @PathVariable(name = "itemId") long itemId,
                                          @Valid @RequestBody CommentDto commentDto) {
        log.debug("Request received POST '/items/{}/comment' : {}", itemId, commentDto);
        log.debug("X-Sharer-User-Id={}'", userId);
        return service.addComment(userId, itemId, commentDto);
    }
}
