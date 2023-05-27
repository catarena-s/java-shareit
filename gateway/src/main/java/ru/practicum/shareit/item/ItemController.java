package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import static ru.practicum.shareit.util.Constants.X_SHARER_USER_ID;

@RestController
@Slf4j
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {

    private final ItemClient itemClient;

    @GetMapping
    public ResponseEntity<Object> getAll(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @RequestParam(name = "from", required = false) @Min(0) Integer from,
                                         @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(50) Integer size) {
        log.debug(X_SHARER_USER_ID, userId);
        if (from == null) {
            log.debug("Request received GET '/items'");
            return itemClient.getAll(userId);
        }
        log.debug("Request received GET '/items?from={}&size={}'", from, size);
        return itemClient.getAll(userId, from, size);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@RequestHeader("X-Sharer-User-Id") long userId,
                                          @PathVariable(name = "itemId") long itemId) {
        log.debug(X_SHARER_USER_ID, userId);
        log.debug("Request received GET '/items/{}'", itemId);
        return itemClient.getItemById(userId, itemId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(@RequestHeader(value = "X-Sharer-User-Id", required = false) long userId,
                                         @Valid @RequestBody ItemDto itemDto) {
        log.debug(X_SHARER_USER_ID, userId);
        log.debug("Request received POST '/items' : {}", itemDto);
        return itemClient.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader(value = "X-Sharer-User-Id", required = false) long userId,
                                         @PathVariable(name = "itemId") long itemId,
                                         @RequestBody ItemDto itemDto) {
        log.debug(X_SHARER_USER_ID, userId);
        log.debug("Request received PATCH '/items/{}' : {}", itemId, itemDto);
        return itemClient.updateItem(userId, itemDto, itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(
            @RequestHeader(value = "X-Sharer-User-Id", required = false) long userId,
            @RequestParam(name = "text") String text,
            @RequestParam(name = "from", required = false) @Min(0) Integer from,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(50) Integer size
    ) {
        log.debug(X_SHARER_USER_ID, userId);
        if (from == null) {
            log.debug("Request received GET '/items/search?text={}'", text);
            return itemClient.search(userId, text);
        }
        log.debug("Request received GET '/items/search?text={}&from={}&size={}'", text, from, size);
        return itemClient.search(userId, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createCommentToItem(@RequestHeader(value = "X-Sharer-User-Id", required = false) long userId,
                                                      @PathVariable(name = "itemId") long itemId,
                                                      @Valid @RequestBody CommentDto commentDto) {
        log.debug(X_SHARER_USER_ID, userId);
        log.debug("Request received POST '/items/{}/comment' : {}", itemId, commentDto);
        return itemClient.createCommentToItem(userId, itemId, commentDto);
    }
}
