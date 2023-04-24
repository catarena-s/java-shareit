package ru.practicum.shareit.request;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * TODO Sprint add-item-requests.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ItemRequest {
    private long id;// уникальный идентификатор
    private String description;// текст запроса, содержащий описание требуемой вещи
    private long requestor;// пользователь, создавший запрос
    private Date created;// дата и время создания запроса
}