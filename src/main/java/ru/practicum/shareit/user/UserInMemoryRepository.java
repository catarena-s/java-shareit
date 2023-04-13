package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class UserInMemoryRepository implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private long lastId = 0;

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public User update(UserDto user, long userId) {
        User updatedUser = users.get(userId);
        if (user.getName() != null) {
            updatedUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            updatedUser.setEmail(user.getEmail());
        }
        return updatedUser;
    }

    @Override
    public User create(UserDto user) {
        User newUser = User.builder()
                .id(getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
        user.setId(newUser.getId());
        users.put(newUser.getId(), newUser);
        return newUser;
    }

    @Override
    public User getById(long userId) {
        return users.get(userId);
    }

    @Override
    public void delete(long userId) {
        users.remove(userId);
    }

    @Override
    public boolean existUser(long userId) {
        return users.containsKey(userId);
    }

    public boolean existEmail(String email) {
        return users.values()
                .stream()
                .anyMatch(f -> isEqualsEmail(email, f));
    }

    @Override
    public boolean existEmail(String email, long userId) {
        return users.values()
                .stream()
                .anyMatch(f -> isEqualsEmail(email, f) && f.getId() != userId);
    }

    private boolean isEqualsEmail(String email, User f) {
        return f.getEmail().equalsIgnoreCase(email);
    }

    private long getId() {
        return ++lastId;
    }
}
