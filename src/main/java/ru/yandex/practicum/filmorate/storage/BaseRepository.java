package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;


@RequiredArgsConstructor
public abstract class BaseRepository<T> {
    protected final JdbcTemplate jdbcTemplate;
    protected final RowMapper<T> mapper;

    protected T findById(String query, Object... params) {
        return jdbcTemplate.queryForObject(query, mapper, params);
    }

    protected List<T> findAll(String query, Object... params) {
        return jdbcTemplate.query(query, mapper, params);
    }
}

