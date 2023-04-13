package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserRepository {
    /**
     * Get all users
     * @return collection of user
     */
    Collection<User> getAll();

    /**
     * Update user's data
     * @param user
     * @param userId
     * @return updates user
     */
    User update(UserDto user, long userId);

    /**
     * Create new user
     * @param user
     * @return new user
     */
    User create(UserDto user);

    /**
     * Get user by id
     * @param userId
     * @return user
     */
    User getById(long userId);

    /**
     * Delete user by id
     * @param userId
     */
    void delete(long userId);

    /**
     * Check user with input id is existing in storage
     * @param userId
     * @return true or false
     */
    boolean existUser(long userId);

    /**
     * Is user with same email is existing in storage
     * @param email
     * @return true or false
     */
    boolean existEmail(String email);

    /**
     * Is other user with same email is existing in storage
     * @param email
     * @param userId
     * @return true or false
     */
    boolean existEmail(String email, long userId);
}
