package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private String name;//краткое название
    private String description;//развёрнутое описание
    private Boolean available;//статус о том, доступна или нет вещь для аренды
    private Long requestId;
}
