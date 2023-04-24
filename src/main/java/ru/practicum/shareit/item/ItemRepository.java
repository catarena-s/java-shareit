package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    /**
     * Get all items by owner
     * @param owner - owner id
     * @return collection of items
     */
    List<Item> findAllByOwner(Long owner);

    /**
     * Check item with itemId is existing in storage for owner with userId
     * @param id item's id
     * @param owner owner's id
     * @return true or false
     */
    boolean existsByIdAndOwner(long id, long owner);

    /**
     * Returns a collection of items for search substring by name or description
     * @param text search substring
     * @return collection of items
     */
    @Query(value = "select it.* from items it " +
            "where it.available=true and " +
            "(UPPER(it.name) like %:text% or UPPER(it.description) like %:text%)",
            nativeQuery = true
    )
    List<Item> findAllByNameOrDescriptionIgnoreCase(@Param("text") String text);

}
