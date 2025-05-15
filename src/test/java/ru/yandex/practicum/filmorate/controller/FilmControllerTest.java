package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.Film;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTest {
    private FilmController filmController;

    @BeforeEach
    void setUp() throws Exception {
        filmController = new FilmController();

        Field filmsField = FilmController.class.getDeclaredField("films");
        filmsField.setAccessible(true);
        filmsField.set(filmController, new java.util.HashMap<>());
    }

    @Test
    void createValidFilm() throws Exception {

        Film validFilm = new Film();
        validFilm.setName("Фильм");
        validFilm.setDescription("Тестовое описание");
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        validFilm.setDuration(120);


        Film createdFilm = filmController.create(validFilm);


        assertNotNull(createdFilm.getId());
        assertEquals("Фильм", createdFilm.getName());
        assertEquals("Тестовое описание", createdFilm.getDescription());
        assertEquals(120, createdFilm.getDuration());
    }

    @Test
    void createFilmWithLongDescription() {
        Film film = new Film();
        film.setName("Тестовый фильм");
        film.setDescription("a".repeat(201));
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120);

        assertThrows(ConditionsNotMetException.class, () -> filmController.create(film));
    }

    @Test
    void createFilmWithSymbolDescription() {
        Film film = new Film();
        film.setName("Тестовый фильм");
        film.setDescription("a".repeat(200));
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120);

        assertDoesNotThrow(() -> filmController.create(film));
    }

    @Test
    void createFilmWithEarlyReleaseDate() {
        Film film = new Film();
        film.setName("Тестовый фильм");
        film.setDescription("Тестовое описание");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(120);

        assertThrows(ConditionsNotMetException.class, () -> filmController.create(film));
    }

    @Test
    void createFilmWithMinReleaseDate() {
        Film film = new Film();
        film.setName("Тестовый фильм");
        film.setDescription("Тестовое описание");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(120);

        assertDoesNotThrow(() -> filmController.create(film));
    }

    @Test
    void updateFilmWithNonExistentId() {
        Film film = new Film();
        film.setId(999L);
        film.setName("Тестовый фильм");
        film.setDescription("Тестовое описание");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120);

        assertThrows(ConditionsNotMetException.class, () -> filmController.update(film));
    }
}