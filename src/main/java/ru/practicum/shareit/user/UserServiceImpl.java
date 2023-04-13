package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import javax.validation.ValidationException;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository storage;

    @Override
    public Collection<ru.practicum.shareit.user.dto.UserDto> getAll() {
        return storage.getAll()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto create(UserDto user) {
        if (storage.existEmail(user.getEmail())) {
            throw new ValidationException(String.format("User with email='%s' already exist", user.getEmail()));
        }
        User newUser = storage.create(user);
        return UserMapper.toUserDto(newUser);
    }

    @Override
    public UserDto update(UserDto user, long userId) {
        if (!existUser(userId)) {
            throw new NotFoundException(String.format("User with id=%s not found", userId));
        }
        if (user.getEmail() != null && storage.existEmail(user.getEmail(), userId)) {
            throw new ValidationException(String.format("Other user with email='%s' already exist", user.getEmail()));
        }

        User updatedUser = storage.update(user, userId);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public boolean existUser(long userId) {
        return storage.existUser(userId);
    }


    @Override
    public UserDto getById(long userId) {
        if (!existUser(userId)) {
            throw new NotFoundException(String.format("User with id=%s not found", userId));
        }
        return UserMapper.toUserDto(storage.getById(userId));
    }

    @Override
    public void delete(long userId) {
        if (!existUser(userId)) {
            throw new NotFoundException(String.format("User with id = %s not found", userId));
        }
        storage.delete(userId);
    }
}
