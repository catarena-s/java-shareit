package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.util.Constants;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.util.Constants.MSG_USER_WITH_ID_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository repository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public List<ItemRequestDtoResponse> getAllByRequestOwner(long userId) {
        if (!userService.existUser(userId)) {
            throw new NotFoundException(String.format(MSG_USER_WITH_ID_NOT_FOUND, userId));
        }
        return repository.findAllByRequesterId(userId).stream()
                .map(ItemRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDtoResponse getById(long userId, long requestId) {
        if (!userService.existUser(userId)) {
            throw new NotFoundException(String.format(MSG_USER_WITH_ID_NOT_FOUND, userId));
        }
        final ItemRequest itemRequest = repository.findById(requestId).orElseThrow(
                () -> new NotFoundException(String.format("Request(id=%d) for owner id=%d not found", requestId, userId)));
        return ItemRequestMapper.toDto(itemRequest);
    }

    @Override
    public ItemRequestDtoResponse create(long userId, ItemRequestDto itemRequestDto) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(MSG_USER_WITH_ID_NOT_FOUND, userId)));

        final LocalDateTime currentTime = LocalDateTime.now();
        itemRequestDto.setCreated(currentTime);

        final ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto, user);
        return ItemRequestMapper.toDto(repository.save(itemRequest));
    }

    @Override
    public List<ItemRequestDtoResponse> getAllFromOtherUsers(long userId, PageRequest page) {
        if (!userService.existUser(userId)) {
            throw new NotFoundException(String.format(MSG_USER_WITH_ID_NOT_FOUND, userId));
        }
        return (page != null)
                ? repository.findAllByRequesterIdNot(userId, page)
                .map(ItemRequestMapper::toDto)
                .getContent()
                : repository.findAllByRequesterIdNot(userId, Constants.SORT_BY_REQUEST_CREATE_DATE_DESC)
                .stream()
                .map(ItemRequestMapper::toDto)
                .collect(Collectors.toList());
    }

}
