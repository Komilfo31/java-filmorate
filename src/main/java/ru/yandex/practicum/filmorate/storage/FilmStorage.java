package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;


import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FilmStorage {
    Film createFilm(Film film);

    Film saveFilm(Film film);

    Film updateFilm(Film film);

    Optional<Film> getFilmById(Long id);

    List<Film> getAllFilms();

    boolean exists(Long id);

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    List<Film> getPopularFilms(int count);


    Set<Genre> getFilmGenres(int filmId);


}
