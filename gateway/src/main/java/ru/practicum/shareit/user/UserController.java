package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserClient userClient;

    @GetMapping
    public ResponseEntity<Object> getAll(@RequestParam(name = "from", required = false) @Min(0) Integer from,
                                         @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(50) Integer size) {
        log.debug("Request received GET '/users'");
        return (from == null) ? userClient.getAllUsers() : userClient.getAllUsers(from, size);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getById(@PathVariable(name = "userId") long userId) {
        log.debug("Request received GET '/users/{}'", userId);
        return userClient.getUserById(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(
            @Valid @RequestBody UserDto userDto) {
        log.debug("Request received POST '/users' : {}", userDto);
        return userClient.createUser(userDto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> update(
            @PathVariable(name = "userId") long userId,
            @RequestBody UserDto userDto) {
        log.debug("Request received PATCH '/users/{}' : {}", userId, userDto);
        return userClient.updateUser(userId, userDto);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable(name = "userId") long userId) {
        log.debug("Request received DELETE '/users/{}'", userId);
        userClient.delete(userId);
    }
}
