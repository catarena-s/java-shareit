package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemDtoShort {
    private long id;//уникальный идентификатор вещи
    private String name;//краткое название
    private String description;//краткое название
    private boolean available;
    private Long requestId;
}
