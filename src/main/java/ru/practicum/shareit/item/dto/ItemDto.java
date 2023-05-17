package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;


@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private long id;//уникальный идентификатор вещи
    @NotBlank(message = "Name cannot be empty or null")
    private String name;//краткое название
    @NotBlank(message = "Description cannot be empty or null")
    private String description;//развёрнутое описание
    @NotNull(message = "Available cannot be null")
    private Boolean available;//статус о том, доступна или нет вещь для аренды
    private Long ownerID;
    private BookingShortDto lastBooking;
    private BookingShortDto nextBooking;
    private List<CommentDto> comments;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long requestId;
}
