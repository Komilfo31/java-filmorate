package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findUsers() {
        log.info("Получен запрос на получение списка всех пользователей. Текущее количество: {}", users.size());
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        log.info("Получен запрос на создание нового пользователя: {}", user);

        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            String error = "Email должен быть указан и содержать символ @";
            log.warn("Ошибка валидации при создании пользователя: {}", error);
            throw new ConditionsNotMetException(error);
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            String error = "Логин не должен быть пустым и содержать пробелы";
            log.warn("Ошибка валидации при создании пользователя: {}", error);
            throw new ConditionsNotMetException(error);
        }

        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пользователя не указано, будет использован логин");
            user.setName(user.getLogin());
        }

        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            String error = "Дата рождения не может быть в будущем";
            log.warn("Ошибка валидации при создании пользователя: {}", error);
            throw new ConditionsNotMetException(error);
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь успешно создан. ID: {}, логин: {}", user.getId(), user.getLogin());
        return user;
    }

    private long getNextId() {
        return users.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0) + 1;
    }


    @PutMapping
    public User update(@RequestBody User newUser) {
        if (newUser.getId() == null) {
            String error = "Id должен быть указан";
            log.warn("Ошибка валидации при обновлении пользователя: {}", error);
            throw new ConditionsNotMetException(error);
        }

        if (!users.containsKey(newUser.getId())) {
            String error = "Пользователь с указанным id не найден";
            log.warn("Ошибка при обновлении пользователя: {}", error);
            throw new ConditionsNotMetException(error);
        }

        User oldUser = users.get(newUser.getId());


        oldUser.setEmail(newUser.getEmail());
        oldUser.setName(newUser.getName());
        oldUser.setLogin(newUser.getLogin());
        oldUser.setBirthday(newUser.getBirthday());
        log.info("Пользователь успешно обновлен. ID: {}, логин: {}", oldUser.getId(), oldUser.getLogin());
        return oldUser;
    }

}
