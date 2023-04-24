package ru.practicum.shareit.user;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * TODO Sprint add-controllers.
 */
@Setter
@Getter
@ToString
@EqualsAndHashCode
@Builder
public class User {
    private long id;
    private String name;
    private String email;
}
