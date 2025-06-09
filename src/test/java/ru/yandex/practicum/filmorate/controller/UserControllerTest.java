package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.UserValidator;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserControllerTest {
    private UserValidator validator = new UserValidator();

    @Test
    void validateEmailIsNull() {
        User user = User.builder().login("valid").build();
        assertThrows(ConditionsNotMetException.class, () -> validator.validate(user));
    }

    @Test
    void validateEmailIsBlank() {
        User user = User.builder().email(" ").login("valid").build();
        assertThrows(ConditionsNotMetException.class, () -> validator.validate(user));
    }

    @Test
    void validateEmailHasNoAt() {
        User user = User.builder().email("invalid").login("valid").build();
        assertThrows(ConditionsNotMetException.class, () -> validator.validate(user));
    }

    @Test
    void validateLoginIsNull() {
        User user = User.builder().email("test@test").build();
        assertThrows(ConditionsNotMetException.class, () -> validator.validate(user));
    }

    @Test
    void validateNameWhenNameIsNull() {
        User user = User.builder()
                .email("test@test")
                .login("login")
                .name(null)
                .build();
        validator.validate(user);
        assertEquals("login", user.getName());
    }
}
