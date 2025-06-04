package ru.yandex.practicum.filmorate.validation;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@Component
public class UserValidator {
    public void validate(User user) {
        if (user == null) {
            throw new ValidationException("User не может быть пустым");
        }
        validateEmail(user.getEmail());
        validateLogin(user.getLogin());
        validateName(user);
        validateBirthday(user.getBirthday());
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ConditionsNotMetException("Email не может быть пустым");
        }
        if (!email.contains("@")) {
            throw new ConditionsNotMetException("Email должен содержать символ @");
        }
    }

    private void validateLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new ConditionsNotMetException("Логин не может быть пустым");
        }
        if (login.contains(" ")) {
            throw new ConditionsNotMetException("Логин не может содержать пробелы");
        }
    }

    private void validateName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void validateBirthday(LocalDate birthday) {
        if (birthday != null && birthday.isAfter(LocalDate.now())) {
            throw new ConditionsNotMetException("Дата рождения не может быть в будущем");
        }
    }
}