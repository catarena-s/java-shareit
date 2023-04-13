package ru.practicum.shareit.request.dto;

import java.util.Date;

/**
 * TODO Sprint add-item-requests.
 */

public class ItemRequestDto {
    private Long id;// уникальный идентификатор
    private String description;// текст запроса, содержащий описание требуемой вещи
    private Long requestor;// пользователь, создавший запрос
    private Date created;// дата и время создания запроса

}
