package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemDtoShort;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ItemRequestDto {
    private Long id;// уникальный идентификатор
    @NotBlank(message = "Description cannot be empty or null")
    private String description;// текст запроса, содержащий описание требуемой вещи
    private Long requester;// пользователь, создавший запрос
    private LocalDateTime created;// дата и время создания запроса
    List<ItemDtoShort> items;
}
