package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.request.ItemRequest;

/**
 * TODO Sprint add-controllers.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
public class Item {
    private long id;//уникальный идентификатор
    private String name;//краткое название
    private String description;//развёрнутое описание
    private boolean available;//статус о том, доступна или нет вещь для аренды
    private long owner;//владелец вещи
    private ItemRequest request;//id пользователя, сделавшего запрос
}
