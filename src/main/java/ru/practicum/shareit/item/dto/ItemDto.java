package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * TODO Sprint add-controllers.
 */
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class ItemDto {
    private long id;//уникальный идентификатор вещи
    @NotBlank(message = "Name cannot be empty")
    private String name;//краткое название
    @NotBlank(message = "Name cannot be empty")
    private String description;//развёрнутое описание
    @NotNull(message = "Available cannot be null")
    private Boolean available;//статус о том, доступна или нет вещь для аренды
    private Long request;//id пользователя, сделавшего запрос
}
