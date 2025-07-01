package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Repository
@Qualifier("dbFilmStorage")
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;
    private final GenreRowMapper genreRowMapper;
    private final MpaRowMapper mpaRowMapper;
    private final GenreService genreService;
    private final MpaStorage mpaStorage;

    private static final String FIND_BY_ID_QUERY = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration,
                   m.id AS mpa_id, m.name AS mpa_name
            FROM films f
            JOIN mpa_ratings m ON f.mpa_rating_id = m.id
            WHERE f.id = ?
            """;

    private static final String FIND_ALL_QUERY = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration,
                   m.id AS mpa_id, m.name AS mpa_name
            FROM films f
            JOIN mpa_ratings m ON f.mpa_rating_id = m.id
            """;

    private static final String INSERT_QUERY = """
            INSERT INTO films (name, description, release_date, duration, mpa_rating_id) 
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_QUERY = """
            UPDATE films 
            SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? 
            WHERE id = ?
            """;

    private static final String GET_POPULAR_FILMS_QUERY = """
            SELECT 
                f.id,
                f.name,
                f.description,
                f.release_date,
                f.duration,
                m.id AS mpa_id,
                m.name AS mpa_name,
                (SELECT COUNT(*) FROM film_likes fl WHERE fl.film_id = f.id) AS likes_count
            FROM films f
            JOIN mpa_ratings m ON f.mpa_rating_id = m.id
            ORDER BY likes_count DESC
            LIMIT ?
            """;

    private static final String GET_GENRES_QUERY = """
            SELECT g.id, g.name 
            FROM film_genres fg 
            JOIN genres g ON fg.genre_id = g.id 
            WHERE fg.film_id = ?
            """;

    private static final String DELETE_GENRES_QUERY = "DELETE FROM film_genres WHERE film_id = ?";
    private static final String INSERT_GENRE_QUERY = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate, FilmRowMapper filmRowMapper,
                         GenreRowMapper genreRowMapper, MpaRowMapper mpaRowMapper,
                         GenreService genreService, MpaStorage mpaStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmRowMapper = filmRowMapper;
        this.genreRowMapper = genreRowMapper;
        this.mpaRowMapper = mpaRowMapper;
        this.genreService = genreService;
        this.mpaStorage = mpaStorage;
    }

    @Override
    @Transactional
    public Film saveFilm(Film film) {
        if (film.getId() == null) {
            return createFilm(film);
        } else {
            return updateFilm(film);
        }
    }

    @Override
    @Transactional
    public Film createFilm(Film film) {
        log.info("Создаём фильм: {}", film.getName());

        if (film.getMpa() == null) {
            throw new ValidationException("Необходимо указать рейтинг MPA");
        }

        int mpaId = film.getMpa().getId();

        Mpa mpa = mpaStorage.getMpaById(mpaId)
                .orElseThrow(() -> new NotFoundException("MPA с id=" + mpaId + " не найден"));

        film.setMpa(mpa);
        mpaStorage.getMpaById(film.getMpa().getId())
                .orElseThrow(() -> new NotFoundException("MPA с id=" + film.getMpa().getId() + " не найден"));

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Genre> uniqueGenres = new LinkedHashSet<>(film.getGenres());
            film.setGenres((Collections.unmodifiableSet(uniqueGenres)));

            for (Genre genre : film.getGenres()) {
                genreService.getGenreById(genre.getId());
            }
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, (int) film.getDuration());
            ps.setLong(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            updateFilmGenres(film);
        }

        return film;
    }

    @Override
    @Transactional
    public Film updateFilm(Film film) {
        log.info("Обновляем фильм с id: {}", film.getId());

        if (!exists(film.getId())) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        mpaStorage.getMpaById(film.getMpa().getId())
                .orElseThrow(() -> new NotFoundException("MPA не найден"));

        int updated = jdbcTemplate.update(UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (updated == 0) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        jdbcTemplate.update(DELETE_GENRES_QUERY, film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            updateFilmGenres(film);
        }

        return getFilmById(film.getId()).orElseThrow();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Film> getFilmById(Long id) {
        log.debug("Получаем фильм по id: {}", id);
        try {
            Film film = jdbcTemplate.queryForObject(FIND_BY_ID_QUERY, filmRowMapper, id);
            if (film != null) {
                Set<Genre> genres = getFilmGenres(film.getId().intValue());
                film.setGenres(genres != null ? genres : new LinkedHashSet<>());
            }
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Film> getAllFilms() {
        log.debug("Получаем все фильмы");
        try {
            List<Film> films = jdbcTemplate.query(FIND_ALL_QUERY, filmRowMapper);
            films.forEach(film -> {
                Set<Genre> genres = getFilmGenres(film.getId().intValue());
                film.setGenres(genres != null ? genres : new LinkedHashSet<>());
            });
            return films;
        } catch (Exception e) {
            log.error("Ошибка при загрузке фильмов", e);
            throw new RuntimeException("Ошибка при загрузке фильмов", e);
        }
    }

    @Override
    public boolean exists(Long id) {
        if (id == null) return false;
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM films WHERE id = ?)",
                Boolean.class,
                id
        );
        return exists != null && exists;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);

    }

    @Override
    @Transactional(readOnly = true)
    public List<Film> getPopularFilms(int limit) {
        log.debug("Получаем популярные {} фильмы", limit);
        try {
            List<Film> films = jdbcTemplate.query(GET_POPULAR_FILMS_QUERY, filmRowMapper, limit);
            films.forEach(film -> {
                Set<Genre> genres = getFilmGenres(film.getId().intValue());
                film.setGenres(genres != null ? genres : new LinkedHashSet<>());
            });
            return films;
        } catch (Exception e) {
            log.error("Ошибка при загрузке популярных фильмов", e);
            throw new RuntimeException("Ошибка при загрузке популярных фильмов", e);
        }
    }


    @Override
    public Set<Genre> getFilmGenres(int filmId) {
        try {
            return new HashSet<>(jdbcTemplate.query(
                    GET_GENRES_QUERY,
                    genreRowMapper,
                    filmId
            ));
        } catch (EmptyResultDataAccessException e) {
            return new HashSet<>();
        }
    }


    private void updateFilmGenres(Film film) {
        jdbcTemplate.batchUpdate(INSERT_GENRE_QUERY, film.getGenres(), film.getGenres().size(),
                (ps, genre) -> {
                    ps.setLong(1, film.getId());
                    ps.setLong(2, genre.getId());
                });
    }
}