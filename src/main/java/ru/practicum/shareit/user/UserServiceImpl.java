package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import javax.validation.ValidationException;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository storage;

    @Override
    public Collection<ru.practicum.shareit.user.dto.UserDto> getAll() {
        return storage.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto create(UserDto user) {
//        if (storage.existEmail(user.getEmail())) {
//            throw new ValidationException(String.format("User with email='%s' already exist", user.getEmail()));
//        }
        User newUser = storage.save(UserMapper.toUser(user));
        return UserMapper.toUserDto(newUser);
    }

    @SuppressWarnings("checkstyle:ParenPad")
    @Override
    public UserDto update(UserDto userDto, long userId) {
        if (!existUser(userId)) {
            throw new NotFoundException(String.format("User with id=%s not found", userId));
        }
        if (userDto.getEmail() != null && storage.existsByIdNotAndEmail(userId, userDto.getEmail())) {
            throw new ValidationException(String.format("Other user with email='%s' already exist", userDto.getEmail()));
        }
        User user = storage.getReferenceById(userId);
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null)
            user.setEmail(userDto.getEmail());
        User updatedUser = storage.save(user);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public boolean existUser(long userId) {
        return storage.existsById(userId);
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
        storage.deleteById(userId);
    }
}
