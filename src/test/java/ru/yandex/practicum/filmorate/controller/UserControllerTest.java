package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController userController;

    @BeforeEach
    void setUp() throws Exception {
        userController = new UserController();
        Field usersField = UserController.class.getDeclaredField("users");
        usersField.setAccessible(true);
        usersField.set(userController, new java.util.HashMap<>());
    }

    @Test
    void createValidUser() throws Exception {
        User validUser = new User();
        validUser.setEmail("testov@email.com");
        validUser.setLogin("testLogin");
        validUser.setName("test Name");
        validUser.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userController.create(validUser);

        assertNotNull(createdUser.getId());
        assertEquals("testov@email.com", createdUser.getEmail());
        assertEquals("testLogin", createdUser.getLogin());
        assertEquals("test Name", createdUser.getName());
    }

    @Test
    void createUserWithNullEmail() {
        User user = new User();
        user.setEmail(null);
        user.setLogin("Тест");
        user.setName("Имя");
        user.setBirthday(LocalDate.now());

        assertThrows(ConditionsNotMetException.class, () -> userController.create(user));
    }

    @Test
    void createUserWithInvalidEmailFormat() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("Тест");
        user.setName("Тестов");
        user.setBirthday(LocalDate.now());

        assertThrows(ConditionsNotMetException.class, () -> userController.create(user));
    }

    @Test
    void createUserWithSpacesInLogin() {
        User user = new User();
        user.setEmail("email@test.com");
        user.setLogin("login with spaces");
        user.setName("Тестов");
        user.setBirthday(LocalDate.now());

        assertThrows(ConditionsNotMetException.class, () -> userController.create(user));
    }

    @Test
    void createUserWithFutureBirthday() {
        User user = new User();
        user.setEmail("email@test.com");
        user.setLogin("Логин");
        user.setName("Тестов");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ConditionsNotMetException.class, () -> userController.create(user));
    }
}
