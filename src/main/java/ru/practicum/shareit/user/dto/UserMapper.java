package ru.practicum.shareit.user.dto;

import ru.practicum.shareit.user.User;

public class UserMapper {
    private UserMapper() {
    }

    public static ru.practicum.shareit.user.dto.UserDto toUserDto(User user) {
        return ru.practicum.shareit.user.dto.UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
