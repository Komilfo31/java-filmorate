package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Optional<Film> getById(Long id);

    Film create(Film film);

    Film update(Film film);

    void delete(Long id);


    Collection<Film> findAll();

    boolean exists(Long id);
}
