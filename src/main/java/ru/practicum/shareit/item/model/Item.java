package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;//уникальный идентификатор
    @Column
    private String name;//краткое название
    @Column
    private String description;//развёрнутое описание
    @Column(name = "available")
    private boolean available;//статус о том, доступна или нет вещь для аренды
    @Column(name = "owner")
    private long owner;//владелец вещи
//    private ItemRequest request;//id пользователя, сделавшего запрос
}
