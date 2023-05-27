package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.practicum.shareit.util.TestValidatorUtil;

class UserDtoValidateTest {
    @ParameterizedTest
    @CsvSource({",'   '", "''"})
    void create_whenNameIsEmptyOrNull(String name) {
        final UserDto userDto = UserDto.builder()
                .name(name)
                .email("email@mail.com")
                .build();
        Assertions.assertAll(
                () -> Assertions.assertTrue(TestValidatorUtil.hasErrorMessage(userDto, "Name cannot be empty or null"))
        );
    }

    @ParameterizedTest
    @CsvSource({",'   '", "''"})
    void create_whenEmailIsEmptyOrNull(String email) {
        final UserDto userDto = UserDto.builder()
                .name("name")
                .email(email)
                .build();

        Assertions.assertTrue(TestValidatorUtil.hasErrorMessage(userDto, "Email cannot be empty or null"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"email", "email @mail.com", "mail.com", "@mail@com"})
    void create_whenNotValidEmail(String email) {
        final UserDto userDto = UserDto.builder()
                .name("name")
                .email(email)
                .build();

        Assertions.assertTrue(TestValidatorUtil.hasErrorMessage(userDto, "Email must be valid"));
    }
}