package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemDtoShortResponse;

import java.time.LocalDateTime;
import java.util.List;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDtoResponse {
    private Long id;// уникальный идентификатор
    private String description;// текст запроса, содержащий описание требуемой вещи
    private Long requester;// пользователь, создавший запрос
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime created;// дата и время создания запроса
    private List<ItemDtoShortResponse> items;
}
