package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

import static ru.practicum.shareit.util.Constants.MSG_USER_WITH_ID_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Collection<UserDto> getAll() {
        return UserMapper.toListDto(repository.findAll());
    }

    @Override
    @Transactional
    public UserDto create(UserDto user) {
        try {
            User newUser = repository.save(UserMapper.toUser(user));
            return UserMapper.toUserDto(newUser);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException(String.format("User with email='%s' already exists", user.getEmail()));
        }
    }

    @Override
    @Transactional
    public UserDto update(UserDto userDto, long userId) {
        if (userDto.getEmail() != null && isExistsOtherUserWithEmail(userDto, userId)) {
            throw new ConflictException(String.format("Another user already exists with email = '%s'", userDto.getEmail()));
        }
        User user = repository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(MSG_USER_WITH_ID_NOT_FOUND, userId)));

        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null)
            user.setEmail(userDto.getEmail());
        User updatedUser = repository.save(user);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getById(long userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(MSG_USER_WITH_ID_NOT_FOUND, userId)));
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public void delete(long userId) {
        if (!existUser(userId)) {
            throw new NotFoundException(String.format(MSG_USER_WITH_ID_NOT_FOUND, userId));
        }
        repository.deleteById(userId);
    }

    @Override
    public boolean existUser(long userId) {
        return repository.existsById(userId);
    }

    private boolean isExistsOtherUserWithEmail(UserDto userDto, long userId) {
        return repository.existsByIdNotAndEmail(userId, userDto.getEmail());
    }
}
