package ru.practicum.shareit.request;

import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    /**
     * @param userId
     * @param itemRequestDto
     * @return
     */
    ItemRequestDto create(long userId, ItemRequestDto itemRequestDto);

    /**
     * @param userId
     * @param requestId
     * @return
     */
    ItemRequestDto getById(long userId, long requestId);

    /**
     * @param userId
     * @return
     */
    List<ItemRequestDto> getAllByRequestOwner(long userId);

    /**
     * @param userId
     * @param page
     * @return
     */
    List<ItemRequestDto> getAllFromOtherUsers(long userId, PageRequest page);
}
