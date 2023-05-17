package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter
@Getter
@Builder
@ToString
@EqualsAndHashCode
public class UserShort {
    private long id;
}
