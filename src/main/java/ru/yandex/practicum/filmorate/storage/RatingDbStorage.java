package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.RatingRowMapper;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RatingDbStorage implements RatingStorage {

    private final JdbcTemplate jdbcTemplate;

    private final RatingRowMapper ratingRowMapper;

    @Override
    public Rating findById(long id) {
        String sql = "SELECT * FROM ratings WHERE rating_id = ?;";
        try {
            return jdbcTemplate.queryForObject(sql, ratingRowMapper, id);
        } catch (Exception e) {
            String errorMessage = "Rating with id " + id + " not found";
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }

    }

    @Override
    public List<Rating> findAll() {
        String sql = "SELECT * FROM ratings;";
        return jdbcTemplate.query(sql, ratingRowMapper);
    }
}
