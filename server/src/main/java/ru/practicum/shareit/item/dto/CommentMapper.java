package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentMapper {

    public static CommentDtoResponse toDto(Comment comment) {
        return CommentDtoResponse.builder()
                .id(comment.getId())
                .itemId(comment.getItem().getId())
                .authorName(comment.getAuthor().getName())
                .text(comment.getText())
                .created(comment.getCreated())
                .build();
    }

    public static Comment toComment(CommentDto commentDto, User user, Item item, LocalDateTime now) {
        return Comment.builder()
                .text(commentDto.getText())
                .author(user)
                .item(item)
                .created(now)
                .build();
    }
}
