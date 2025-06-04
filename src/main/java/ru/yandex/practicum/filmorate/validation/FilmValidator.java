package ru.yandex.practicum.filmorate.validation;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;

@Component
public class FilmValidator {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION_LENGTH = 200;

    public FilmValidator(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public void validate(Film film) {
        validateName(film.getName());
        validateDescription(film.getDescription());
        validateReleaseDate(film.getReleaseDate());
        validateDuration(film.getDuration());
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ConditionsNotMetException("Название фильма не должно быть пустым");
        }
    }

    private void validateDescription(String description) {
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new ConditionsNotMetException("Описание не должно превышать 200 символов");
        }
    }

    private void validateReleaseDate(LocalDate releaseDate) {
        if (releaseDate == null) {
            throw new ConditionsNotMetException("Дата релиза должна быть указана");
        }
        if (releaseDate.isBefore(MIN_RELEASE_DATE)) {
            throw new ConditionsNotMetException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }

    private void validateDuration(float duration) {
        if (duration <= 0) {
            throw new ConditionsNotMetException("Продолжительность фильма должна быть положительной");
        }
    }

    public void validateFilmExists(long filmId) {
        if (!filmStorage.exists(filmId)) {
            throw new NotFoundException("Фильм с ID=" + filmId + " не найден");
        }
    }

    public void validateUserExists(long userId) {
        if (!userStorage.exists(userId)) {
            throw new NotFoundException("Пользователь с ID=" + userId + " не найден");
        }
    }
}