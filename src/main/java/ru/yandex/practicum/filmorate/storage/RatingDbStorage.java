package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.List;

@Repository
public class RatingDbStorage extends BaseRepository<Rating> implements RatingStorage {

    private static final String FIND_ALL_QUERY = """
            SELECT * FROM ratings;
            """;

    private static final String FIND_BY_ID_QUERY = """
            SELECT * FROM ratings WHERE rating_id = ?
            """;

    @Autowired
    public RatingDbStorage(JdbcTemplate jdbcTemplate, RowMapper<Rating> mapper) {
        super(jdbcTemplate, mapper);
    }

    @Override
    public Rating findById(long id) {
        return findById(FIND_BY_ID_QUERY, id);
    }

    @Override
    public List<Rating> findAll() {
        return findAll(FIND_ALL_QUERY);
    }
}
