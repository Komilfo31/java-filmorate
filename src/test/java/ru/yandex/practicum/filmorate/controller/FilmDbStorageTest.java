package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.db.FilmDbStorage;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorageTest {
    private final FilmDbStorage filmDbStorage;
    private final MpaStorage mpaStorage;
    private final GenreService genreService;
    private Film film;
    private Mpa mpa;

    @BeforeEach
    public void setUp() {
        mpa = mpaStorage.getMpaById(1)
                .orElseThrow(() -> new NotFoundException("MPA не найден"));

        film = Film.builder()
                .name("Sopranos")
                .duration(100)
                .description("Description")
                .releaseDate(LocalDate.parse("1996-11-25"))
                .mpa(mpa)
                .build();
    }

    @DisplayName("Создание фильма с основными полями")
    @Test
    public void testCreateFilm() {
        Film savedFilm = filmDbStorage.createFilm(film);

        assertThat(savedFilm).isNotNull();
        assertThat(savedFilm.getId()).isNotNull();
        assertEquals(film.getName(), savedFilm.getName());
        assertEquals(film.getDescription(), savedFilm.getDescription());
        assertEquals(film.getReleaseDate(), savedFilm.getReleaseDate());
        assertEquals(mpa.getId(), savedFilm.getMpa().getId());
    }

    @DisplayName("Получение фильма по id")
    @Test
    public void testGetFilmById() {
        Film savedFilm = filmDbStorage.createFilm(film);

        Optional<Film> foundFilmOptional = filmDbStorage.getFilmById(savedFilm.getId());

        assertTrue(foundFilmOptional.isPresent());
        Film foundFilm = foundFilmOptional.get();

        assertEquals(savedFilm.getId(), foundFilm.getId());
        assertEquals(savedFilm.getName(), foundFilm.getName());
        assertEquals(savedFilm.getDescription(), foundFilm.getDescription());
        assertEquals(savedFilm.getReleaseDate(), foundFilm.getReleaseDate());
        assertEquals(savedFilm.getMpa().getId(), foundFilm.getMpa().getId());
    }

    @DisplayName("Обновление фильма")
    @Test
    public void testUpdateFilm() {
        Film newFilm = Film.builder()
                .name("Original Name")
                .duration(100)
                .description("Original Description")
                .releaseDate(LocalDate.parse("1996-11-25"))
                .mpa(mpa)
                .build();

        Film createdFilm = filmDbStorage.createFilm(newFilm);
        assertNotNull(createdFilm.getId(), "Фильм должен получить ID при создании");

        Film filmToUpdate = Film.builder()
                .id(createdFilm.getId()) // Используем ID созданного фильма
                .name("Updated Name")
                .duration(120)
                .description("Updated Description")
                .releaseDate(LocalDate.parse("1997-12-26"))
                .mpa(mpaStorage.getMpaById(2).orElseThrow())
                .build();

        Genre genre = genreService.getGenreById(1);
        filmToUpdate.setGenres(new LinkedHashSet<>(Collections.singletonList(genre)));

        Film updatedFilm = filmDbStorage.updateFilm(filmToUpdate);

        assertEquals(createdFilm.getId(), updatedFilm.getId(), "ID должен остаться прежним");
        assertEquals("Updated Name", updatedFilm.getName());
        assertEquals("Updated Description", updatedFilm.getDescription());
        assertEquals(120, updatedFilm.getDuration());
        assertEquals(LocalDate.parse("1997-12-26"), updatedFilm.getReleaseDate());
        assertEquals(2, updatedFilm.getMpa().getId());
        assertEquals(1, updatedFilm.getGenres().size());
        assertTrue(updatedFilm.getGenres().contains(genre));
    }

    @DisplayName("Получение списка всех фильмов")
    @Test
    public void testGetAllFilms() {
        Film savedFilm = filmDbStorage.createFilm(film);

        List<Film> films = filmDbStorage.getAllFilms();

        assertFalse(films.isEmpty());
        assertTrue(films.stream().anyMatch(f -> f.getId().equals(savedFilm.getId())));
    }

    @DisplayName("Получение популярных фильмов")
    @Test
    public void testGetPopularFilms() {
        Film savedFilm = filmDbStorage.createFilm(film);

        List<Film> popularFilms = filmDbStorage.getPopularFilms(10);

        assertFalse(popularFilms.isEmpty());
        assertTrue(popularFilms.stream().anyMatch(f -> f.getId().equals(savedFilm.getId())));
    }
}