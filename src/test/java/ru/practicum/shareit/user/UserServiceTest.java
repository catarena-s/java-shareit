package ru.practicum.shareit.user;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.TestInitDataUtil;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.util.Constants;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.TestInitDataUtil.makeUser;
import static ru.practicum.shareit.util.Constants.MSG_USER_WITH_ID_NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository repository;
    @InjectMocks
    private UserServiceImpl service;
    private List<User> userList;
    private List<UserDto> userDtoList;

    @BeforeEach
    void setUp() {
        userList = TestInitDataUtil.getUserList();
        userDtoList = (List<UserDto>) UserMapper.toListDto(userList);
    }

    @Test
    void getById_whenUserFound_thenReturnUser() {
        final User expectsUser = userList.get(0);
        final UserDto expectsUserDto = UserMapper.toUserDto(expectsUser);

        when(repository.findById(anyLong()))
                .thenReturn(Optional.of(expectsUser));

        final UserDto actualUser = service.getById(1L);

        assertEquals(expectsUserDto, actualUser);
    }

    @Test
    void getById_whenUserNotFound_throwException() {
        long userId = 1L;

        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.getById(userId));

        assertEquals(String.format(MSG_USER_WITH_ID_NOT_FOUND, userId), exception.getMessage());
    }

    @Test
    void create_whenUserEmailUnique_throwException() {
        UserDto dto = UserDto.builder().id(1L).name("Jon").email("jon@mail.com").build();
        User user = makeUser(1L, "Jon", "jon@mail.com");
        when(repository.save(any())).thenReturn(user);

        final UserDto actualNewUser = service.create(dto);
        assertEquals(dto, actualNewUser);
    }

    @Test
    void create_whenUserEmailNotUnique_throwException() {
        UserDto dto = UserDto.builder().name("Jon").email("jon@mail.com").build();

        when(repository.save(any()))
                .thenThrow(DataIntegrityViolationException.class);

        final ConflictException conflictException = assertThrows(ConflictException.class, () -> service.create(dto));
        assertEquals("User with email='jon@mail.com' already exists", conflictException.getMessage());
    }

    @Test
    void getAll_withoutPagination() {
        when(repository.findAll()).thenReturn(userList);

        final Collection<UserDto> actualList = service.getAll(null);

        assertFalse(actualList.isEmpty());
        assertEquals(3, actualList.size());
        assertEquals(userDtoList, actualList);
    }

    @Test
    void getAll_withPagination() {
        when(repository.findAll((Pageable) any()))
                .thenAnswer(invocation -> {
                    final Pageable argument = invocation.getArgument(0, Pageable.class);
                    if (argument.getPageSize() <= 3) {
                        if (argument.getPageNumber() == 0) {
                            if (argument.getPageSize() == 1) {
                                return new PageImpl<>(List.of(userList.get(0)));
                            }
                            if (argument.getPageSize() == 2) {
                                return new PageImpl<>(List.of(userList.get(0), userList.get(1)));
                            } else
                                return new PageImpl<>(userList);
                        }
                        if (argument.getPageNumber() == 1) {
                            if (argument.getPageSize() == 1) {
                                return new PageImpl<>(List.of(userList.get(1)));
                            }
                            if (argument.getPageSize() == 2) {
                                return new PageImpl<>(List.of(userList.get(2)));
                            }
                            return new PageImpl<>(Collections.emptyList());
                        }
                        if (argument.getPageNumber() == 2) {
                            if (argument.getPageSize() == 1) {
                                return new PageImpl<>(List.of(userList.get(2)));
                            }
                        }
                        return Collections.emptyList();
                    }
                    if (argument.getPageNumber() == 0) return new PageImpl<>(userList);
                    else return Collections.emptyList();
                });

        PageRequest page = PageRequest.of(0, 1, Constants.SORT_BY_ID_ACS);
        final Collection<UserDto> actualList = service.getAll(page);

        assertFalse(actualList.isEmpty());
        assertEquals(1, actualList.size());
        assertEquals(List.of(userDtoList.get(0)), actualList);

        page = PageRequest.of(0, 3, Constants.SORT_BY_ID_ACS);
        final Collection<UserDto> actualList2 = service.getAll(page);
        assertFalse(actualList2.isEmpty());
        assertEquals(3, actualList2.size());
        assertEquals(userDtoList, actualList2);

        page = PageRequest.of(1, 2, Constants.SORT_BY_ID_ACS);
        final Collection<UserDto> actualList3 = service.getAll(page);
        assertFalse(actualList3.isEmpty());
        assertEquals(1, actualList3.size());
        assertEquals(List.of(userDtoList.get(2)), actualList3);

        page = PageRequest.of(1, 3, Constants.SORT_BY_ID_ACS);
        final Collection<UserDto> actualList4 = service.getAll(page);
        assertTrue(actualList4.isEmpty());
    }

    @Test
    void update_whenUserDataCorrect() {
        User user = userList.get(0);
        UserDto dto = UserDto.builder().name("Jonny").email("jon123@mail.com").build();
        User updatedUser = user.toBuilder().name("Jonny").email("jon123@mail.com").build();
        UserDto expectsUserDto = UserMapper.toUserDto(updatedUser);
        final long userId = 1L;

        when(repository.existsByIdNotAndEmail(anyLong(), anyString())).thenReturn(false);
        when(repository.findById(anyLong())).thenReturn(Optional.of(user));
        when(repository.save(any())).thenReturn(updatedUser);

        final UserDto actualNewUser = service.update(dto, userId);
        assertEquals(expectsUserDto, actualNewUser);

        verify(repository, times(1)).save(updatedUser);
        verify(repository, times(1)).findById(userId);
    }

    @ParameterizedTest
    @CsvSource({
            "      , m1@email.tu ",
            " Nike ,            ",
            "      ,            "
    })
    void update_whenUserNameAneEmailIsNull(String name, String email) {
        User user = User.builder().name("Make").email("m@email.tu").build();
        UserDto dto = UserDto.builder().name(name).email(email).build();
        User updatedUser = user.toBuilder().build();
        if (name != null) updatedUser.setName(name);
        if (email != null) updatedUser.setEmail(email);
        UserDto expectsUserDto = UserMapper.toUserDto(updatedUser);

        lenient().when(repository.existsByIdNotAndEmail(anyLong(), anyString())).thenReturn(false);
        when(repository.findById(anyLong())).thenReturn(Optional.of(user));
        when(repository.save(any())).thenReturn(updatedUser);

        final UserDto actualNewUser = service.update(dto, 1L);
        assertEquals(expectsUserDto, actualNewUser);

        verify(repository, times(1)).findById(1L);
        verify(repository, atMost(1)).existsByIdNotAndEmail(user.getId(), email);
        verify(repository, times(1)).save(updatedUser);
    }

    @Test
    void update_whenUserEmailNotUnique_throwException() {
        User user = userList.get(0);
        UserDto dto = UserDto.builder().email("jon123@mail.com").build();
        final long userId = 1L;

        when(repository.existsByIdNotAndEmail(anyLong(), anyString())).thenReturn(true);

        final ConflictException actualException = assertThrows(ConflictException.class,
                () -> service.update(dto, userId));

        assertEquals("Another user already exists with email = 'jon123@mail.com'", actualException.getMessage());

        verify(repository, never()).save(user);
        verify(repository, never()).findById(userId);
    }

    @Test
    void update_whenUserNotExists_throwException() {
        final long userId = 1L;
        final UserDto dto = UserDto.builder()
                .name("Janet")
                .email("jon123@mail.com")
                .build();

        when(repository.existsByIdNotAndEmail(anyLong(), anyString())).thenReturn(false);
        when(repository.findById(anyLong())).thenReturn(Optional.empty());


        final NotFoundException actualException = assertThrows(NotFoundException.class,
                () -> service.update(dto, userId));

        assertEquals(String.format(MSG_USER_WITH_ID_NOT_FOUND, userId), actualException.getMessage());

        verify(repository, times(1)).findById(userId);
        verify(repository, times(1)).existsByIdNotAndEmail(userId, "jon123@mail.com");
        verify(repository, never()).save(new User());
    }

    @SneakyThrows
    @Test
    void delete_whenUserExists() {
        final long userId = 1L;

        when(repository.existsById(anyLong())).thenReturn(true);
        doNothing().when(repository).deleteById(anyLong());

        service.delete(userId);

        verify(repository, times(1)).deleteById(userId);
    }


    @Test
    void delete_whenUserNotExists_throwException() {
        when(repository.existsById(anyLong())).thenReturn(false);

        final long userId = 1L;
        final NotFoundException actualException = assertThrows(NotFoundException.class,
                () -> service.delete(userId));

        assertEquals(String.format(MSG_USER_WITH_ID_NOT_FOUND, userId), actualException.getMessage());

        verify(repository, never()).deleteById(userId);
    }

    @Test
    void existUser_whenUserExist_thenReturnTrue() {
        when(repository.existsById(anyLong())).thenReturn(true);

        final boolean userExistUser = service.existUser(1L);

        assertTrue(userExistUser);
    }

    @Test
    void existUser_whenUserNotExist_thenReturnFalse() {
        when(repository.existsById(anyLong())).thenReturn(false);

        final boolean userExistUser = service.existUser(1L);

        assertFalse(userExistUser);
    }
}