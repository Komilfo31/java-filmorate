package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    User create(User user);

    User update(User user);

    void delete(Long id);

    User getById(Long id);

    Collection<User> findAll();

    boolean exists(Long id);
}
