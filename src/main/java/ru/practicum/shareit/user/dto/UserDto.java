package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@EqualsAndHashCode
@Builder
public class UserDto {
    private Long id;
    @NotBlank(message = "Name cannot be empty or null")
    private String name;
    @NotBlank(message = "Email cannot be empty or null")
    @Email(message = "Email must be valid")
    private String email;
}
