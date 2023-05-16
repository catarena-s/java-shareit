package ru.practicum.shareit.request;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.util.Constants;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
class ItemRequestRepositoryTest {
    @Autowired
    private ItemRequestRepository requestRepository;
    @Autowired
    private UserRepository userRepository;
    private List<ItemRequest> requestList;
    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .name("Jon").email("jon@mail.ru")
                .build());

        final User user2 = userRepository.save(User.builder()
                .name("Jane").email("jane@mail.ru")
                .build());

        final ItemRequest request1 = getRequest("Would like to use a brush", user);
        final ItemRequest request2 = getRequest("Would like to use a screwdriver", user2);

        requestList = List.of(request1, request2);
    }

    @AfterEach
    void afterEach() {
        requestRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    void findAllByRequesterId() {
        final long userId = user.getId();
        final Optional<List<ItemRequest>> actualRequests =
                Optional.ofNullable(requestRepository.findAllByRequesterId(userId));

        assertThat(actualRequests)
                .isPresent()
                .hasValueSatisfying(itemRequests -> {
                    assertThat(itemRequests).isNotEmpty();
                    assertThat(itemRequests).hasSize(1);
                    assertThat(itemRequests).element(0)
                            .hasFieldOrPropertyWithValue("id", requestList.get(0).getId());
                    assertThat(itemRequests).element(0)
                            .hasFieldOrPropertyWithValue("description", "Would like to use a brush");
                });
    }

    @Test
    void findAllByRequesterIdNot() {
        final long userId = user.getId();
        final Optional<List<ItemRequest>> actualRequests = Optional.ofNullable(
                requestRepository.findAllByRequesterIdNot(userId, Constants.SORT_BY_REQUEST_CREATE_DATE_DESC));

        assertThat(actualRequests)
                .isPresent()
                .hasValueSatisfying(itemRequests -> {
                    assertThat(itemRequests).isNotEmpty();
                    assertThat(itemRequests).hasSize(1);
                    assertThat(itemRequests).element(0)
                            .hasFieldOrPropertyWithValue("id", requestList.get(1).getId());
                    assertThat(itemRequests).element(0)
                            .hasFieldOrPropertyWithValue("description", "Would like to use a screwdriver");
                });
    }

    @Test
    void testFindAllByRequesterIdNotWithPagination() {
        final long userId = user.getId();
        PageRequest page = PageRequest.of(0, 20, Constants.SORT_BY_REQUEST_CREATE_DATE_DESC);
        final Optional<Page<ItemRequest>> actualRequests = Optional.ofNullable(
                requestRepository.findAllByRequesterIdNot(userId, page));

        assertThat(actualRequests)
                .isPresent()
                .hasValueSatisfying(itemRequests -> {
                    assertThat(itemRequests.getContent()).isNotEmpty();
                    assertThat(itemRequests.getContent()).hasSize(1);
                    assertThat(itemRequests.getContent()).element(0)
                            .hasFieldOrPropertyWithValue("id", requestList.get(1).getId());
                    assertThat(itemRequests.getContent()).element(0)
                            .hasFieldOrPropertyWithValue("description", "Would like to use a screwdriver");
                });
    }

    @NotNull
    private ItemRequest getRequest(String description, User author) {
        return requestRepository.save(ItemRequest.builder()
                .description(description)
                .requester(author)
                .created(LocalDateTime.now())
                .build());
    }
}