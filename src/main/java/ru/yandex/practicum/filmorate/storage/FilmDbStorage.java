package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository("filmDbStorage")
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {

    private static final String FIND_ALL_QUERY = """
            SELECT f.*,
                   r.name AS rating_name,
                   GROUP_CONCAT(DISTINCT (g.genre_id || ':' || g.name)) AS genre_data,
                   GROUP_CONCAT(DISTINCT l.user_id) AS likes
            FROM films f
            LEFT JOIN ratings r ON f.rating_id = r.rating_id
            LEFT JOIN film_genres fg ON f.film_id = fg.film_id
            LEFT JOIN genres g ON fg.genre_id = g.genre_id
            LEFT JOIN likes l ON f.film_id = l.film_id
            GROUP BY f.film_id, r.name
            """;
    private static final String FIND_BY_ID_QUERY = """
            SELECT f.*,
                   r.name AS rating_name,
                   GROUP_CONCAT(DISTINCT (g.genre_id || ':' || g.name)) AS genre_data,
                   GROUP_CONCAT(DISTINCT l.user_id) AS likes
            FROM films f
            LEFT JOIN ratings r ON f.rating_id = r.rating_id
            LEFT JOIN film_genres fg ON f.film_id = fg.film_id
            LEFT JOIN genres g ON fg.genre_id = g.genre_id
            LEFT JOIN likes l ON f.film_id = l.film_id
            WHERE f.film_id = ?
            GROUP BY f.film_id, r.name
            """;

    private static final String INSERT_QUERY = """
                INSERT INTO films (name, description, release_date, duration, rating_id)
                VALUES (?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_QUERY = """
            UPDATE films
            SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ?
            WHERE film_id = ?
            """;

    String INSERT_GENRE_QUERY = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

    String DELETE_LIKE_QUERY = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

    String INSERT_LIKE_QUERY = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<Film> findAll() {

        try {
            return findAll(FIND_ALL_QUERY);
        } catch (EmptyResultDataAccessException e) {
            String errorMessage = "No films found";
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }

    }


    @Override
    public Film findById(long id) {
        try {
            return findById(FIND_BY_ID_QUERY,id);
        } catch (Exception e) {
            String errorMessage = "Film with id " + id + " not found";
            log.error("Could not find film: {}", e.getMessage());
            throw new NotFoundException(errorMessage);
        }
    }

    @Override
    public Film addFilm(Film film) {

        long id = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration().toMinutes(),
                film.getMpa().getId()
        );
        film.setId(id);
        insertGenres(film);
        return film;

    }

    @Override
    public Film updateFilm(Film film) {

        try {
            update(UPDATE_QUERY, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration().toMinutes(), film.getMpa().getId(), film.getId());
        } catch (Exception e) {
            String errorMessage = "Cannot update film: " + e.getMessage();
            log.error("Update failed: {}", e.getMessage());
            throw new ValidationException(errorMessage);
        }
        return film;
    }

    @Override
    public void addLike(long filmId, long userId) {

        try {
            int rowsUpdated = jdbcTemplate.update(INSERT_LIKE_QUERY, filmId, userId);
            if (rowsUpdated == 0) {
                String errorMessage = "Cannot add like";
                log.error("Cannot add like from user with id: {} to film with id: {}", userId, filmId);
                throw new RuntimeException(errorMessage);
            }
        } catch (Exception e) {
            String errorMessage = "Cannot add like: " + e.getMessage();
            log.error("Add like failed: {}", errorMessage);
            throw new ValidationException(errorMessage);
        }

    }

    @Override
    public void removeLike(long filmId, long userId) {

        try {
            int rowsDeleted = jdbcTemplate.update(DELETE_LIKE_QUERY, filmId, userId);
            if (rowsDeleted == 0) {
                String errorMessage = "Cannot not delete like";
                log.error("Cannot not delete like from user with id: {} from film with id: {}", userId, filmId);
                throw new RuntimeException(errorMessage);
            }
        } catch (Exception e) {
            String errorMessage = "Cannot remove like: " + e.getMessage();
            log.error("Remove like failed: {}", errorMessage);
            throw new ValidationException(errorMessage);
        }

    }

    private void insertGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) return;

        List<Object[]> batchArgs = film.getGenres().stream()
                .map(genre -> new Object[]{film.getId(), genre.getId()})
                .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(INSERT_GENRE_QUERY, batchArgs);
    }

}