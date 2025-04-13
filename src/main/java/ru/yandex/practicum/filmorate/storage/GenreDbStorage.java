package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    private final GenreRowMapper genreRowMapper;

    @Override
    public Genre getGenre(long id) {
        String sql = "SELECT * FROM genres WHERE genre_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, genreRowMapper, id);
        } catch (Exception e) {
            String errorMessage = "Genre with id " + id + " not found";
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }

    }

    @Override
    public List<Genre> getGenres() {
        String sql = "SELECT * FROM genres order by genre_id asc";
        return jdbcTemplate.query(sql, genreRowMapper);
    }
}
