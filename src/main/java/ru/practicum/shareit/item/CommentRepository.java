package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    /**
     * Find comments for item
     * @param itemId
     * @return
     */
    List<Comment> findByItemId(long itemId);

    /**
     * Find all comments for items
      * @param items
     * @return
     */
    List<Comment> findAllByItemIn(List<Item> items);

}
