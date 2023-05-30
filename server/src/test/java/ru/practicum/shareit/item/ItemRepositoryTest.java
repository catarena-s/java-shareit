package ru.practicum.shareit.item;

import org.assertj.core.api.ListAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.practicum.shareit.TestInitDataUtil.getItemList;
import static ru.practicum.shareit.TestInitDataUtil.getUserList;
import static ru.practicum.shareit.util.Constants.SORT_BY_ID_ACS;

@DataJpaTest
class ItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    private List<User> users;
    private List<Item> items;

    @BeforeEach
    void init() {
        users = getUserList(userRepository);
        items = getItemList(itemRepository, users);
    }

    @Test
    void findByIdAndOwnerId() {
        User expectedOwner = users.get(0);
        Item expectedItem = items.get(0);
        final Optional<Item> foundItem = itemRepository.findByIdAndOwnerId(expectedItem.getId(), expectedOwner.getId());
        assertThat(foundItem)
                .isPresent()
                .hasValueSatisfying(item -> {
                            assertThat(item).hasFieldOrPropertyWithValue("id", expectedItem.getId());
                            assertThat(item).hasFieldOrPropertyWithValue("name", expectedItem.getName());
                            assertThat(item).hasFieldOrPropertyWithValue("description", expectedItem.getDescription());
                            assertThat(item.getOwner()).hasFieldOrPropertyWithValue("id", expectedOwner.getId());
                        }
                );
    }

    @ParameterizedTest
    @CsvSource({"1, 0", "0, 1"})
    void findByIdAndOwnerId_returnEmpty(int ownerIndex, int itemIndex) {
        User expectedOwner = users.get(ownerIndex);
        Item expectedItem = items.get(itemIndex);
        final Optional<Item> foundItem = itemRepository.findByIdAndOwnerId(expectedItem.getId(), expectedOwner.getId());
        assertThat(foundItem).isEmpty();
    }

    @Test
    void findAllByOwnerId() {
        User expectedOwner = users.get(1);
        final Optional<List<Item>> foundItemList = Optional.ofNullable(itemRepository.findAllByOwnerId(expectedOwner.getId(), SORT_BY_ID_ACS));
        assertThat(foundItemList)
                .isPresent()
                .hasValueSatisfying(itemList -> {
                    assertThat(itemList).isNotEmpty();
                    assertThat(itemList).hasSize(2);
                    assertThat(itemList).element(0).hasFieldOrPropertyWithValue("id", items.get(1).getId());
                    assertThat(itemList).element(0).hasFieldOrPropertyWithValue("name", items.get(1).getName());
                    assertThat(itemList).element(1).hasFieldOrPropertyWithValue("id", items.get(2).getId());
                    assertThat(itemList).element(1).hasFieldOrPropertyWithValue("name", items.get(2).getName());
                });
    }

    @Test
    void findAllByOwnerIdWithPagination() {
        PageRequest page = PageRequest.of(0, 1);
        User expectedOwner = users.get(1);
        final Optional<Page<Item>> foundItemList = Optional.ofNullable(itemRepository.findAllByOwnerId(expectedOwner.getId(), page));
        assertThat(foundItemList)
                .isPresent()
                .hasValueSatisfying(pageItems -> {
                    final List<Item> content = pageItems.getContent();
                    final ListAssert<Item> itemListAssert = assertThat(content);
                    itemListAssert.isNotEmpty();
                    itemListAssert.hasSize(1);
                    itemListAssert.element(0).hasFieldOrPropertyWithValue("id", items.get(1).getId());
                    itemListAssert.element(0).hasFieldOrPropertyWithValue("name", items.get(1).getName());
                });
    }

    @Test
    void findAllByNameOrDescriptionIgnoreCase() {
        final Optional<List<Item>> foundItemList = Optional.ofNullable(itemRepository.findAllByNameOrDescriptionIgnoreCase("item2", SORT_BY_ID_ACS));
        assertThat(foundItemList)
                .isPresent()
                .hasValueSatisfying(itemList -> {
                    assertThat(itemList).isNotEmpty();
                    assertThat(itemList).hasSize(2);
                    assertThat(itemList).element(0).hasFieldOrPropertyWithValue("id", items.get(1).getId());
                    assertThat(itemList).element(0).hasFieldOrPropertyWithValue("name", items.get(1).getName());
                    assertThat(itemList).element(1).hasFieldOrPropertyWithValue("id", items.get(2).getId());
                    assertThat(itemList).element(1).hasFieldOrPropertyWithValue("name", items.get(2).getName());
                });
    }

    @Test
    void findAllByNameOrDescriptionIgnoreCaseWithPagination() {
        PageRequest page = PageRequest.of(0, 2);
        final Optional<Page<Item>> foundItemList =
                Optional.ofNullable(itemRepository.findAllByNameOrDescriptionIgnoreCase("item", page));
        assertThat(foundItemList)
                .isPresent()
                .hasValueSatisfying(pageItems -> {
                    final List<Item> content = pageItems.getContent();
                    final ListAssert<Item> itemListAssert = assertThat(content);
                    itemListAssert.isNotEmpty();
                    itemListAssert.hasSize(2);
                    itemListAssert.element(0).hasFieldOrPropertyWithValue("id", items.get(0).getId());
                    itemListAssert.element(0).hasFieldOrPropertyWithValue("name", items.get(0).getName());
                    itemListAssert.element(1).hasFieldOrPropertyWithValue("id", items.get(1).getId());
                    itemListAssert.element(1).hasFieldOrPropertyWithValue("name", items.get(1).getName());
                });
    }
}