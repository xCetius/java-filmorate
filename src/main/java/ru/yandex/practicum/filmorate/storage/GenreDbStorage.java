package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Repository
@Slf4j
public class GenreDbStorage extends BaseRepository<Genre> implements GenreStorage {

    private static final String FIND_ALL_QUERY = """
            SELECT genre_id, name FROM genres order by genre_id asc
            """;

    private static final String FIND_BY_ID_QUERY = """
            SELECT genre_id, name FROM genres WHERE genre_id = ?
            """;

    @Autowired
    public GenreDbStorage(JdbcTemplate jdbcTemplate, RowMapper<Genre> mapper) {
        super(jdbcTemplate, mapper);
    }

    @Override
    public Genre findById(long id) {
        return findById(FIND_BY_ID_QUERY, id);
    }

    @Override
    public List<Genre> findAll() {
        return findAll(FIND_ALL_QUERY);
    }
}
