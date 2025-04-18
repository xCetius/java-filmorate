package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;


@RequiredArgsConstructor
public abstract class BaseRepository<T> {
    protected final JdbcTemplate jdbcTemplate;
    protected final RowMapper<T> mapper;

    protected T findById(String query, Object... params) {

        try {
            return jdbcTemplate.queryForObject(query, mapper, params);
        } catch (EmptyResultDataAccessException e) {
            String errorMessage = "Entity with id " + params[0] + " not found";
            throw new NotFoundException(errorMessage);
        }

    }

    protected List<T> findAll(String query, Object... params) {
        return jdbcTemplate.query(query, mapper, params);
    }

    protected long insert(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        }, keyHolder);

        return Objects.requireNonNull(keyHolder.getKeyAs(Long.class), "Failed to retrieve generated ID");
    }

    protected void update(String query, Object... params) {
        jdbcTemplate.update(query, params);
    }
}

