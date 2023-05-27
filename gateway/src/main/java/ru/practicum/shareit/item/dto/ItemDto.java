package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    @NotBlank(message = "Name cannot be empty or null")
    private String name;//краткое название
    @NotBlank(message = "Description cannot be empty or null")
    private String description;//развёрнутое описание
    @NotNull(message = "Available cannot be null")
    private Boolean available;//статус о том, доступна или нет вещь для аренды
    private Long requestId;
}
