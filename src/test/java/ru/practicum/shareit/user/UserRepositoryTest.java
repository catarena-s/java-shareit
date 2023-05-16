package ru.practicum.shareit.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.TestInitDataUtil;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    private List<User> users;

    @BeforeEach
    void addUsers() {
        users = TestInitDataUtil.getUserList(repository);
//        final User user1 = repository.save(User.builder()
//                .name("Jon").email("jon@mail.ru")
//                .build());
//        final User user2 = repository.save(User.builder()
//                .name("Jane").email("jane@mail.ru")
//                .build());
//
//        users = List.of(user1, user2);
    }

    @ParameterizedTest
    @CsvSource({"0, jon@mail.ru, false", "1, jon@mail.ru, true"})
    void existsByIdNotAndEmail(int id, String email, boolean expects) {
        final User user = users.get(id);
        final boolean actualExists = repository.existsByIdNotAndEmail(user.getId(), email);
        assertEquals(expects, actualExists);
    }

    @AfterEach
    void deleteUser() {
        repository.deleteAll();
    }
}