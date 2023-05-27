package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDto {
    private String description;// текст запроса, содержащий описание требуемой вещи
    private LocalDateTime created;// дата и время создания запроса
}
