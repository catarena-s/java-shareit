package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import static ru.practicum.shareit.util.Constants.X_SHARER_USER_ID;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @GetMapping
    public ResponseEntity<Object> getAll(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.debug(X_SHARER_USER_ID, userId);
        log.debug("Request received GET '/requests'");
        return itemRequestClient.getAllByRequestOwner(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable("requestId") long requestId
    ) {
        log.debug(X_SHARER_USER_ID, userId);
        log.debug("Request received GET '/requests/{}'", requestId);
        return itemRequestClient.getById(userId, requestId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @Valid @RequestBody ItemRequestDto itemRequestDto
    ) {
        log.debug(X_SHARER_USER_ID, userId);
        log.debug("Request received POST '/requests'");
        return itemRequestClient.createRequest(userId, itemRequestDto);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllFromOtherUser(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(name = "from", required = false) @Min(0) Integer from,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(50) Integer size
    ) {
        log.debug(X_SHARER_USER_ID, userId);
        if (from == null) {
            log.debug("Request received GET '/requests/all'");
            return itemRequestClient.getAll(userId);
        }
        log.debug("Request received GET '/requests/all?from={}&size={}", from, size);
        return itemRequestClient.getAll(userId, from, size);
    }
}
