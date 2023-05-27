package ru.practicum.shareit.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Is other user with same email is existing in storage
     * @param id
     * @param email
     * @return true or false
     */
    boolean existsByIdNotAndEmail(Long id, String email);
}
