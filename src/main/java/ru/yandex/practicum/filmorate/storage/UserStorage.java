package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User createUser(User user);

    User saveUser(User user);

    User updateUser(User user);

    Optional<User> getUserById(Long id);

    List<User> getAllUsers();

    boolean exists(Long id);

    List<User> getCommonFriends(long userId, long otherId);

    void addFriend(long userId, long friendId);

    void removeFriend(long userId, long friendId);

    List<User> getFriends(long userId);
}
