package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDtoShort;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ItemDtoResponse {
    private long id;//уникальный идентификатор вещи
    private String name;//краткое название
    private String description;//развёрнутое описание
    private Boolean available;//статус о том, доступна или нет вещь для аренды
    private BookingDtoShort lastBooking;
    private BookingDtoShort nextBooking;
    private List<CommentDtoResponse> comments;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long requestId;
}
