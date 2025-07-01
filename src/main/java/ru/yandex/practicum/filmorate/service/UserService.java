package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.AlreadyFriendsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;
    private final UserValidator userValidator;

    @Autowired
    public UserService(@Qualifier("dbUserStorage") UserStorage userStorage, UserValidator userValidator) {
        this.userStorage = userStorage;
        this.userValidator = userValidator;
    }

    @Transactional
    public User createUser(User user) {
        userValidator.validate(user);
        return userStorage.saveUser(user);
    }

    @Transactional
    public User updateUser(User user) {
        userValidator.validate(user);
        return userStorage.updateUser(user);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }


    @Transactional
    public void addFriend(long userId, long friendId) {
        if (userId == friendId) {
            throw new ValidationException("Нельзя добавить себя в друзья");
        }

        // Проверяем существование пользователей
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        userStorage.getUserById(friendId)
                .orElseThrow(() -> new NotFoundException("Друг не найден"));

        userStorage.addFriend(userId, friendId);
    }

    @Transactional
    public void removeFriend(long userId, long friendId) {
        userStorage.removeFriend(userId, friendId);
    }


    @Transactional(readOnly = true)
    public List<User> getFriends(long userId) {
        return userStorage.getFriends(userId);
    }

    @Transactional(readOnly = true)
    public List<User> getCommonFriends(long userId, long otherId) {
        return userStorage.getCommonFriends(userId, otherId);
    }
}
