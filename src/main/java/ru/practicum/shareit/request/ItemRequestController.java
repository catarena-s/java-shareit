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

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

import static ru.practicum.shareit.util.Constants.SORT_BY_REQUEST_CREATE_DATE_DESC;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {

    private final ItemRequestService service;

    @GetMapping
    public List<ItemRequestDto> getAll(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.debug("Request received GET '/items'");
        log.debug("X-Sharer-User-Id={}", userId);
        return service.getAllByRequestOwner(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getById(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable("requestId") long requestId
    ) {
        log.debug("Request received GET '/items'");
        log.debug("X-Sharer-User-Id={}", userId);
        return service.getById(userId, requestId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto create(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @Valid @RequestBody ItemRequestDto itemRequestDto
    ) {
        return service.create(userId, itemRequestDto);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllFromOtherUser(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(name = "from", required = false) @Min(0) Integer from,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(50) Integer size
    ) {
        if (from == null) {
            return service.getAllFromOtherUsers(userId, null);
        }
        final PageRequest page = PageRequest.of(from / size, size, SORT_BY_REQUEST_CREATE_DATE_DESC);
        return service.getAllFromOtherUsers(userId, page);
    }
}
