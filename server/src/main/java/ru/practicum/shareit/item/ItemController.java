package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
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
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoResponse;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static ru.practicum.shareit.util.Constants.SORT_BY_ID_ACS;
import static ru.practicum.shareit.util.Constants.X_SHARER_USER_ID;

@RestController
@Slf4j
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {

    private final ItemService service;

    @GetMapping
    public Collection<ItemDtoResponse> getAll(@RequestHeader("X-Sharer-User-Id") long userId,
                                              @RequestParam(name = "from", required = false) Integer from,
                                              @RequestParam(name = "size", defaultValue = "20") Integer size) {
        log.debug(X_SHARER_USER_ID, userId);
        if (from == null) {
            log.debug("Request received GET '/items'");
            return service.getAllByOwner(userId, null);
        }
        log.debug("Request received GET '/items?from={}&size={}'", from, size);
        final PageRequest page = PageRequest.of(from / size, size, SORT_BY_ID_ACS);
        return service.getAllByOwner(userId, page);
    }

    @GetMapping("/{itemId}")
    public ItemDtoResponse getById(@RequestHeader("X-Sharer-User-Id") long userId,
                                   @PathVariable(name = "itemId") long itemId) {
        log.debug(X_SHARER_USER_ID, userId);
        log.debug("Request received GET '/items/{}'", itemId);
        return service.getById(itemId, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDtoResponse create(@RequestHeader(value = "X-Sharer-User-Id", required = false) long userId,
                                  @RequestBody ItemDto itemDto) {
        log.debug(X_SHARER_USER_ID, userId);
        log.debug("Request received POST '/items' : {}", itemDto);
        return service.create(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDtoResponse update(@RequestHeader(value = "X-Sharer-User-Id", required = false) long userId,
                                  @PathVariable(name = "itemId") long itemId,
                                  @RequestBody ItemDto itemDto) {
        log.debug(X_SHARER_USER_ID, userId);
        log.debug("Request received PATCH '/items/{}' : {}", itemId, itemDto);
        return service.update(itemDto, itemId, userId);
    }

    @GetMapping("/search")
    public List<ItemDtoResponse> search(
            @RequestHeader(value = "X-Sharer-User-Id", required = false) long userId,
            @RequestParam(name = "text") String text,
            @RequestParam(name = "from", required = false) Integer from,
            @RequestParam(name = "size", defaultValue = "20") Integer size
    ) {
        log.debug(X_SHARER_USER_ID, userId);
        if (text.isBlank()) {
            log.debug("Parameter 'text' is empty");
            return Collections.emptyList();
        }
        if (from == null) {
            log.debug("Request received GET '/items/search?text={}'", text);
            return service.search(text, null);
        }
        log.debug("Request received GET '/items/search?text={}&fom{}&size={}'", text, from, size);
        final PageRequest page = PageRequest.of(from / size, size, SORT_BY_ID_ACS);
        return service.search(text, page);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDtoResponse createCommentToItem(@RequestHeader(value = "X-Sharer-User-Id", required = false) long userId,
                                                  @PathVariable(name = "itemId") long itemId,
                                                  @RequestBody CommentDto commentDto) {
        log.debug(X_SHARER_USER_ID, userId);
        log.debug("Request received POST '/items/{}/comment' : {}", itemId, commentDto);
        return service.addComment(userId, itemId, commentDto);
    }
}
