package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public List<User> findAll() {
        return new ArrayList<>(userStorage.findAll());
    }

    public User create(User user) {
        validateUser(user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        User savedUser = userStorage.create(user);
        if (savedUser.getId() == null) {
            throw new IllegalStateException("Не удалось сохранить пользователя");
        }
        return savedUser;
    }

    public User update(User user) {
        validateUser(user);

        if (!userStorage.exists(user.getId())) {
            throw new NotFoundException("Пользователь с ID " + user.getId() + " не найден");
        }

        return userStorage.update(user);
    }

    public void addFriend(Long userId, Long friendId) {
        try {
            validateUsersExist(userId, friendId);

            if (userId.equals(friendId)) {
                throw new ValidationException("Нельзя добавить себя в друзья");
            }

            User user = userStorage.getById(userId);
            User friend = userStorage.getById(friendId);

            if (user.getFriends().contains(friendId)) {
                return;
            }

            user.getFriends().add(friendId);
            friend.getFriends().add(userId);
        } catch (NotFoundException e) {
            throw new NotFoundException("Один из пользователей не найден");
        }
    }


    public void removeFriend(Long userId, Long friendId) {
        try {
            validateUsersExist(userId, friendId);

            User user = userStorage.getById(userId);
            User friend = userStorage.getById(friendId);

            if (!user.getFriends().contains(friendId)) {
                return;
            }

            user.getFriends().remove(friendId);
            friend.getFriends().remove(userId);
        } catch (NotFoundException e) {
            throw new NotFoundException("Один из пользователей не найден");
        }
    }

    public List<User> getFriends(Long userId) {
        try {
            User user = userStorage.getById(userId);
            return user.getFriends().stream()
                    .map(userStorage::getById)
                    .collect(Collectors.toList());
        } catch (NotFoundException e) {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    public List<User> getCommonFriends(Long userId1, Long userId2) {
        try {
            User user1 = userStorage.getById(userId1);
            User user2 = userStorage.getById(userId2);

            return user1.getFriends().stream()
                    .filter(user2.getFriends()::contains)
                    .map(userStorage::getById)
                    .collect(Collectors.toList());
        } catch (NotFoundException e) {
            throw new NotFoundException("Один из пользователей не найден");
        }
    }

    private void validateUsersExist(Long... ids) {
        for (Long id : ids) {
            if (!userStorage.exists(id)) {
                throw new NotFoundException("Пользователь с ID " + id + " не найден");
            }
        }
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new IllegalArgumentException("Email должен быть указан и содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new IllegalArgumentException("Логин не должен быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Дата рождения не может быть в будущем");
        }
    }
}
