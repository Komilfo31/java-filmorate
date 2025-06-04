package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validation.FilmValidator;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FilmControllerTest {
    private final FilmStorage filmStorage = new SimpleFilmStorage();
    private final UserStorage userStorage = new SimpleUserStorage();
    private final FilmValidator filmValidator = new FilmValidator(filmStorage, userStorage);

    @Test
    void validateFilmExists_ShouldThrowNotFoundException_WhenFilmDoesNotExist() {
        assertThrows(NotFoundException.class, () -> filmValidator.validateFilmExists(999L));
    }

    @Test
    void validateFilmExists_ShouldNotThrow_WhenFilmExists() {
        // Добавляем тестовый фильм в хранилище
        ((SimpleFilmStorage) filmStorage).addFilm(1L);
        assertDoesNotThrow(() -> filmValidator.validateFilmExists(1L));
    }

    @Test
    void validateUserExists_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        assertThrows(NotFoundException.class, () -> filmValidator.validateUserExists(999L));
    }

    @Test
    void validateUserExists_ShouldNotThrow_WhenUserExists() {
        // Добавляем тестового пользователя в хранилище
        ((SimpleUserStorage) userStorage).addUser(1L);
        assertDoesNotThrow(() -> filmValidator.validateUserExists(1L));
    }

    //реализация FilmStorage для тестов
    private static class SimpleFilmStorage implements FilmStorage {
        private Set<Long> films = new HashSet<>();

        void addFilm(Long id) {
            films.add(id);
        }

        @Override
        public Film saveFilm(Film film) {
            return null;
        }

        @Override
        public Film updateFilm(Film film) {
            return null;
        }

        @Override
        public Optional<Film> getFilmById(Long id) {
            return Optional.empty();
        }

        @Override
        public List<Film> getAllFilms() {
            return List.of();
        }

        @Override
        public boolean exists(Long id) {
            return films.contains(id);
        }

        @Override
        public void addLike(Long filmId, Long userId) {

        }

        @Override
        public void removeLike(Long filmId, Long userId) {

        }

        @Override
        public List<Film> getPopularFilms(int count) {
            return List.of();
        }
    }

    // реализация UserStorage для тестов
    private static class SimpleUserStorage implements UserStorage {
        private Set<Long> users = new HashSet<>();

        void addUser(Long id) {
            users.add(id);
        }

        @Override
        public User saveUser(User user) {
            return null;
        }

        @Override
        public User updateUser(User user) {
            return null;
        }

        @Override
        public Optional<User> getUserById(Long id) {
            return Optional.empty();
        }

        @Override
        public List<User> getAllUsers() {
            return List.of();
        }

        @Override
        public boolean exists(Long id) {
            return users.contains(id);
        }
    }
}