package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.db.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class})
class UserDbStorageTest {
    private final UserDbStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@mail.com")
                .login("testlogin")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void createUser_ShouldReturnUserWithId() {
        User created = userStorage.createUser(testUser);

        assertThat(created.getId()).isNotNull();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ?",
                Integer.class,
                created.getId()
        )).isEqualTo(1);
    }

    @Test
    void getUserById_ReturnUser() {
        User created = userStorage.createUser(testUser);
        Optional<User> found = userStorage.getUserById(created.getId());

        assertThat(found)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .ignoringFields("friends", "friendships")
                .isEqualTo(created);
    }

    @Test
    void testGetUserById() {
        assertThat(userStorage.getUserById(9999L)).isEmpty();
    }

    @Test
    void testUpdateUser() {
        User created = userStorage.createUser(testUser);
        User updated = User.builder()
                .id(created.getId())
                .email("updated@mail.com")
                .login("updatedlogin")
                .name("Updated Name")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();

        User result = userStorage.updateUser(updated);

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("friends", "friendships")
                .isEqualTo(updated);
    }

    @Test
    void testGetAllUsers() {
        userStorage.createUser(testUser);
        userStorage.createUser(User.builder()
                .email("another@mail.com")
                .login("anotherlogin")
                .birthday(LocalDate.now())
                .build());

        assertThat(userStorage.getAllUsers())
                .usingRecursiveComparison()
                .ignoringFields("id", "friends", "friendships")
                .isEqualTo(List.of(
                        testUser,
                        User.builder()
                                .email("another@mail.com")
                                .login("anotherlogin")
                                .birthday(LocalDate.now())
                                .build()
                ));
    }
}
