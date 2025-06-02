package ru.yandex.practicum.filmorate.validation;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

@Component
public class FilmValidator {
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION_LENGTH = 200;

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
}