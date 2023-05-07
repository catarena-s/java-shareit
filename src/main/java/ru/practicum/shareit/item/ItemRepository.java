package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    /**
     * Get all items by owner
     * @param owner - owner id
     * @return collection of items
     */
    List<Item> findAllByOwnerId(Long owner);

    /**
     * Find by id and owner
     * @param itemId item
     * @param userId owner
     * @return item
     */
    Optional<Item> findByIdAndOwnerId(long itemId, long userId);

    /**
     * Returns a collection of items for search substring by name or description
     * @param text search substring
     * @return collection of items
     */
    @Query("select it from Item it " +
            "where it.available=true and " +
            "(UPPER(it.name) like UPPER(concat('%', :text,'%')) or " +
            "UPPER(it.description) like UPPER(concat('%', :text,'%')))"
    )
    List<Item> findAllByNameOrDescriptionIgnoreCase(@Param("text") String text);
}
