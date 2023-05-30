package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
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
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;

import java.util.List;

import static ru.practicum.shareit.util.Constants.SORT_BY_REQUEST_CREATE_DATE_DESC;
import static ru.practicum.shareit.util.Constants.X_SHARER_USER_ID;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {

    private final ItemRequestService service;

    @GetMapping
    public List<ItemRequestDtoResponse> getAll(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.debug(X_SHARER_USER_ID, userId);
        log.debug("Request received GET '/requests'");
        return service.getAllByRequestOwner(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDtoResponse getById(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable("requestId") long requestId
    ) {
        log.debug(X_SHARER_USER_ID, userId);
        log.debug("Request received GET '/requests/{}'", requestId);
        return service.getById(userId, requestId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDtoResponse create(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestBody ItemRequestDto itemRequestDto
    ) {
        log.debug(X_SHARER_USER_ID, userId);
        log.debug("Request received POST '/requests'");
        return service.create(userId, itemRequestDto);
    }

    @GetMapping("/all")
    public List<ItemRequestDtoResponse> getAllFromOtherUser(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(name = "from", required = false) Integer from,
            @RequestParam(name = "size", defaultValue = "20") Integer size
    ) {
        log.debug(X_SHARER_USER_ID, userId);
        if (from == null) {
            log.debug("Request received GET '/requests/all'");
            return service.getAllFromOtherUsers(userId, null);
        }
        log.debug("Request received GET '/requests/all?from={}&size={}", from, size);
        final PageRequest page = PageRequest.of(from / size, size, SORT_BY_REQUEST_CREATE_DATE_DESC);
        return service.getAllFromOtherUsers(userId, page);
    }
}
