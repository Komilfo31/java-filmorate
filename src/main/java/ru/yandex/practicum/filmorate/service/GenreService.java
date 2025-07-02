package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreStorage genreStorage;
    private final GenreRowMapper genreRowMapper;
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public GenreService(GenreStorage genreStorage, JdbcTemplate jdbcTemplate, GenreRowMapper genreRowMapper) {
        this.genreStorage = genreStorage;
        this.jdbcTemplate = jdbcTemplate;
        this.genreRowMapper = genreRowMapper;
    }

    public List<Genre> getAllGenres() {
        return genreStorage.getAllGenres();
    }

    public Genre getGenreById(int id) {
        return genreStorage.getGenreById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с ID=" + id + " не найден!"));
    }

    private boolean genreExists(int genreId) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM genres WHERE id = ?)",
                Boolean.class,
                genreId
        );
        return exists != null && exists;
    }

    public List<Genre> getGenresByIds(Collection<Integer> genreIds) {
        if (genreIds.isEmpty()) {
            return Collections.emptyList();
        }

        String placeholders = String.join(",", Collections.nCopies(genreIds.size(), "?"));

        String sql = String.format("SELECT id, name FROM genres WHERE id IN (%s)", placeholders);

        Object[] params = genreIds.toArray();

        return jdbcTemplate.query(sql, genreRowMapper, params);
    }
}
