package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findFilms() {
        log.info("Получен запрос на получение списка всех фильмов. Текущее количество: {}", films.size());
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Получен запрос на создание нового фильма: {}", film);

        if (film.getName() == null || film.getName().isBlank()) {
            String error = "Название фильма не должно быть пустым";
            log.warn("Ошибка валидации при создании фильма: {}", error);
            throw new ConditionsNotMetException(error);
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            String error = "Описание не должно превышать 200 символов";
            log.warn("Ошибка валидации при создании фильма: {}", error);
            throw new ConditionsNotMetException(error);
        }

        LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(minReleaseDate)) {
            String error = "Дата релиза не может быть меньше 28.12.1895";
            log.warn("Ошибка валидации при создании фильма: {}", error);
            throw new ConditionsNotMetException(error);
        }

        if (film.getDuration() <= 0) {
            String error = "Продолжительность фильма должна быть положительным числом";
            log.warn("Ошибка валидации при создании фильма: {}", error);
            throw new ConditionsNotMetException(error);
        }

        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм успешно создан. ID: {}, название: {}", film.getId(), film.getName());
        return film;

    }

    private long getNextId() {
        return films.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0) + 1;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        log.info("Получен запрос на обновление фильма: {}", newFilm);

        if (newFilm.getId() == null) {
            String error = "Id должен быть указан";
            log.warn("Ошибка валидации при обновлении фильма: {}", error);
            throw new ConditionsNotMetException(error);
        }

        // Проверка существования фильма
        Film oldFilm = films.get(newFilm.getId());
        if (oldFilm == null) {
            throw new ConditionsNotMetException("Фильм с указанным id не найден");
        }

        oldFilm.setName(newFilm.getName());
        oldFilm.setDescription(newFilm.getDescription());
        oldFilm.setReleaseDate(newFilm.getReleaseDate());
        oldFilm.setDuration(newFilm.getDuration());

        return oldFilm;
    }


}
