package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import javax.validation.ValidationException;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Collection<ru.practicum.shareit.user.dto.UserDto> getAll() {
        return UserMapper.toListDto(repository.findAll());
    }

    @Override
    @Transactional
    public UserDto create(UserDto user) {
        User newUser = repository.save(UserMapper.toUser(user));
        return UserMapper.toUserDto(newUser);
    }

    @Override
    @Transactional
    public UserDto update(UserDto userDto, long userId) {
        if (userDto.getEmail() != null && isExistsOtherUserWithEmail(userDto, userId)) {
            throw new ValidationException(String.format("Other user with email='%s' already exist", userDto.getEmail()));
        }
        User user = repository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d not found", userId)));
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null)
            user.setEmail(userDto.getEmail());
        User updatedUser = repository.save(user);
        return UserMapper.toUserDto(updatedUser);
    }

    private boolean isExistsOtherUserWithEmail(UserDto userDto, long userId) {
        return repository.existsByIdNotAndEmail(userId, userDto.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existUser(long userId) {
        return repository.existsById(userId);
    }


    @Override
    @Transactional(readOnly = true)
    public UserDto getById(long userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d not found", userId)));
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public void delete(long userId) {
        if (!existUser(userId)) {
            throw new NotFoundException(String.format("User with id = %s not found", userId));
        }
        repository.deleteById(userId);
    }
}
