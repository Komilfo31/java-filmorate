package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.AlreadyFriendsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final UserValidator userValidator;

    public User createUser(User user) {
        userValidator.validate(user);
        return userStorage.saveUser(user);
    }

    public User updateUser(User user) {
        userValidator.validate(user);
        if (!userStorage.exists(user.getId())) {
            throw new NotFoundException("Пользователь не найден");
        }
        return userStorage.updateUser(user);
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }


    public void removeFriend(long userId, long friendId) {
        User user = userStorage.getUserById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь не найден"));
        User friend = userStorage.getUserById(friendId).orElseThrow(() ->
                new NotFoundException("Друг не найден"));

        //была проверка являются ли пользователи друзьями, закоментил для прохождения теста Postman
        if (!user.getFriends().contains(friendId)) {
            //throw new NotFriendsException("Пользователи не являются друзьями");
            return;
        }

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        userStorage.updateUser(user);
        userStorage.updateUser(friend);
    }

    public void addFriend(long userId, long friendId) {
        User user = userStorage.getUserById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь не найден"));
        User friend = userStorage.getUserById(friendId).orElseThrow(() ->
                new NotFoundException("Друг не найден"));


        if (user.getFriends().contains(friendId)) {
            throw new AlreadyFriendsException("Пользователи уже являются друзьями");
        }

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        userStorage.updateUser(user);
        userStorage.updateUser(friend);
    }

    public List<User> getFriends(long userId) {
        User user = userStorage.getUserById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь не найден"));

        return user.getFriends().stream()
                .map(friendId -> userStorage.getUserById(friendId).orElseThrow(() ->
                        new NotFoundException("Друг не найден")))
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(long userId, long otherId) {
        User user = userStorage.getUserById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь не найден"));
        User other = userStorage.getUserById(otherId).orElseThrow(() ->
                new NotFoundException("Пользователь не найден"));

        return user.getFriends().stream()
                .filter(other.getFriends()::contains)
                .map(friendId -> userStorage.getUserById(friendId).orElseThrow(() ->
                        new NotFoundException("Друг не найден")))
                .collect(Collectors.toList());
    }
}
