package ru.practicum.shareit.request;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    /**
     * @param userId
     * @return
     */
    List<ItemRequest> findAllByRequesterId(long userId);

    /**
     * @param userId
     * @param sortOrder
     * @return
     */
    List<ItemRequest> findAllByRequesterIdNot(long userId, Sort sortOrder);

    /**
     * @param userId
     * @param page
     * @return
     */
    Page<ItemRequest> findAllByRequesterIdNot(long userId, PageRequest page);
}
