package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.validation.FilmValidator;


import java.util.List;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final FilmValidator filmValidator;

    @Autowired
    public FilmService(@Qualifier("dbFilmStorage") FilmStorage filmStorage, FilmValidator filmValidator) {
        this.filmStorage = filmStorage;
        this.filmValidator = filmValidator;
    }

    @Transactional
    public Film createFilm(Film film) {
        // Проверяем, не существует ли уже фильм с таким ID
        if (film.getId() != null && filmStorage.exists(film.getId())) {
            throw new AlreadyExistsException("Фильм с ID " + film.getId() + " уже существует");
        }

        return filmStorage.createFilm(film);
    }

    @Transactional
    public Film updateFilm(Film film) {
        // Проверяем существование фильма
        if (!filmStorage.exists(film.getId())) {
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
        }

        return filmStorage.updateFilm(film);
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм не найден"));
    }

    public void addLike(Long filmId, Long userId) {
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }
}