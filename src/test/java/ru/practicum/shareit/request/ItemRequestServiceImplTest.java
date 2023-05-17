package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.TestInitDataUtil;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.TestInitDataUtil.getUserList;
import static ru.practicum.shareit.util.Constants.MSG_USER_WITH_ID_NOT_FOUND;
import static ru.practicum.shareit.util.Constants.SORT_BY_REQUEST_CREATE_DATE_DESC;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;
    private List<User> userList;
    private List<ItemRequest> requestList;
    private final Clock clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"));

    @BeforeEach
    void setUp() {
        itemRequestService.setClock(clock);
        userList = getUserList();
        final LocalDateTime currentTime = LocalDateTime.now(clock);
        requestList = TestInitDataUtil.getRequestList(currentTime, userList);
    }

    @Test
    void getById() {
        final User user = userList.get(0);
        final long userId = user.getId();
        final ItemRequest request = requestList.get(0);
        final long requestId = request.getId();

        when(userService.existUser(anyLong())).thenReturn(true);
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(request));

        final ItemRequestDto actualRequest = itemRequestService.getById(userId, requestId);

        assertEquals(ItemRequestMapper.toDto(request),actualRequest);

        verify(userService, times(1)).existUser(userId);
        verify(itemRequestRepository, times(1)).findById(requestId);
    }

    @Test
    void getById_whenNotExistUser_throwException() {
        final long userId = 5L;
        final long requestId = 1L;

        when(userService.existUser(anyLong())).thenReturn(false);

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.getById(userId, requestId));

        assertEquals(String.format(MSG_USER_WITH_ID_NOT_FOUND, userId), exception.getMessage());

        verify(userService, times(1)).existUser(userId);
        verify(itemRequestRepository, never()).findById(requestId);
    }

    @Test
    void getById_whenNotExistRequest_throwException() {
        final User user = userList.get(0);
        final long userId = user.getId();
        final long requestId = 5L;
        final String expectedMessage = String.format("Request(id=%d) for owner id=%d not found", requestId, userId);

        when(userService.existUser(anyLong())).thenReturn(true);
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.getById(userId, requestId));

        assertEquals(expectedMessage, exception.getMessage());

        verify(userService, times(1)).existUser(userId);
        verify(itemRequestRepository, times(1)).findById(requestId);
    }

    @Test
    void getAllByRequestOwner() {
        final User user = userList.get(0);
        final long userId = user.getId();
        final List<ItemRequest> exitedRequestList = List.of(requestList.get(0), requestList.get(2));
        final List<ItemRequestDto> exitedDtoList = List.of(
                ItemRequestMapper.toDto(requestList.get(0)),
                ItemRequestMapper.toDto(requestList.get(2)));

        when(userService.existUser(anyLong())).thenReturn(true);
        when(itemRequestRepository.findAllByRequesterId(anyLong()))
                .thenReturn(exitedRequestList);

        final List<ItemRequestDto> actualRequestList = itemRequestService.getAllByRequestOwner(userId);
        assertIterableEquals(exitedDtoList, actualRequestList);

        verify(userService, times(1)).existUser(userId);
        verify(itemRequestRepository, times(1)).findAllByRequesterId(userId);
    }

    @Test
    void getAllByRequestOwner_whenNotExistUser_throwException() {
        final long userId = 5L;

        when(userService.existUser(anyLong())).thenReturn(false);

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.getAllByRequestOwner(userId));
        assertEquals(String.format(MSG_USER_WITH_ID_NOT_FOUND, userId), exception.getMessage());

        verify(userService, times(1)).existUser(userId);
        verify(itemRequestRepository, never()).findAllByRequesterId(userId);
    }

    @Test
    void getAllByRequestOwner_whenReturnEmptyRequestList() {
        final User user = userList.get(1);
        final long userId = user.getId();

        when(userService.existUser(anyLong())).thenReturn(true);
        when(itemRequestRepository.findAllByRequesterId(anyLong()))
                .thenReturn(Collections.emptyList());

        final List<ItemRequestDto> actualRequestList = itemRequestService.getAllByRequestOwner(userId);
        assertIterableEquals(Collections.emptyList(), actualRequestList);

        verify(userService, times(1)).existUser(userId);
        verify(itemRequestRepository, times(1)).findAllByRequesterId(userId);
    }

    @Test
    void getAllFromOtherUsers() {
        final User user = userList.get(1);
        final long userId = user.getId();
        final List<ItemRequest> exitedRequestList = List.of(requestList.get(0), requestList.get(2));
        final List<ItemRequestDto> exitedDtoList = List.of(
                ItemRequestMapper.toDto(requestList.get(0)),
                ItemRequestMapper.toDto(requestList.get(2)));

        when(userService.existUser(anyLong())).thenReturn(true);
        when(itemRequestRepository.findAllByRequesterIdNot(anyLong(), any(Sort.class)))
                .thenReturn(exitedRequestList);

        final List<ItemRequestDto> actualRequestList = itemRequestService.getAllFromOtherUsers(userId, null);

        assertIterableEquals(exitedDtoList, actualRequestList);

        verify(userService, times(1)).existUser(userId);
        verify(itemRequestRepository, times(1)).findAllByRequesterIdNot(userId, SORT_BY_REQUEST_CREATE_DATE_DESC);
    }

    @Test
    void getAllFromOtherUsers_withPagination() {
        final PageRequest page = PageRequest.of(0, 1, SORT_BY_REQUEST_CREATE_DATE_DESC);
        final User user = userList.get(1);
        final long userId = user.getId();
        final List<ItemRequest> exitedRequestList = List.of(requestList.get(0), requestList.get(2));
        final List<ItemRequestDto> exitedDtoList = List.of(
                ItemRequestMapper.toDto(requestList.get(0)),
                ItemRequestMapper.toDto(requestList.get(2)));

        when(userService.existUser(anyLong())).thenReturn(true);
        when(itemRequestRepository.findAllByRequesterIdNot(anyLong(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(exitedRequestList));

        final List<ItemRequestDto> actualRequestList = itemRequestService.getAllFromOtherUsers(userId, page);

        assertIterableEquals(exitedDtoList, actualRequestList);

        verify(userService, times(1)).existUser(userId);
        verify(itemRequestRepository, times(1))
                .findAllByRequesterIdNot(userId, page);
    }

    @Test
    void getAllFromOtherUsers_whenUserNotExist_throwException() {
        final long userId = 5L;

        when(userService.existUser(anyLong())).thenReturn(false);

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.getAllFromOtherUsers(userId, null));

        assertEquals(String.format(MSG_USER_WITH_ID_NOT_FOUND, userId), exception.getMessage());

        verify(userService, times(1)).existUser(userId);
        verify(itemRequestRepository, never()).findAllByRequesterIdNot(userId, SORT_BY_REQUEST_CREATE_DATE_DESC);
    }

    @Test
    void create() {
        LocalDateTime currentTime = LocalDateTime.now(clock);

        final User user = userList.get(0);
        long userId = user.getId();
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Request description")
                .build();
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(requestDto, user);
        itemRequest.setCreated(currentTime);
        final ItemRequestDto exitedRequestDto = ItemRequestMapper.toDto(itemRequest);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestRepository.save(any())).thenReturn(itemRequest);

        final ItemRequestDto actualRequest = itemRequestService.create(userId, requestDto);
        assertEquals(exitedRequestDto, actualRequest);

        verify(userRepository, times(1)).findById(userId);
        verify(itemRequestRepository, times(1)).save(itemRequest);
    }

    @Test
    void create_whenUserNotExist_throwException() {
        final long userId = 5L;
        final ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Request description")
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.create(userId, requestDto));
        assertEquals(String.format(MSG_USER_WITH_ID_NOT_FOUND, userId), exception.getMessage());

        verify(userRepository, times(1)).findById(userId);
        verify(itemRequestRepository, never()).save(new ItemRequest());
    }
}