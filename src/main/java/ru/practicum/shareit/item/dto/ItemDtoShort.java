package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ItemDtoShort {
    private long id;//уникальный идентификатор вещи
    private String name;//краткое название
}
