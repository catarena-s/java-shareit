package ru.practicum.shareit.request;

import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;

import java.util.List;

public interface ItemRequestService {
    /**
     * @param userId
     * @param itemRequestDto
     * @return
     */
    ItemRequestDtoResponse create(long userId, ItemRequestDto itemRequestDto);

    /**
     * @param userId
     * @param requestId
     * @return
     */
    ItemRequestDtoResponse getById(long userId, long requestId);

    /**
     * @param userId
     * @return
     */
    List<ItemRequestDtoResponse> getAllByRequestOwner(long userId);

    /**
     * @param userId
     * @param page
     * @return
     */
    List<ItemRequestDtoResponse> getAllFromOtherUsers(long userId, PageRequest page);
}
