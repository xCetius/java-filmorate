package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository("filmDbStorage")
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {

    private static final String FIND_ALL_QUERY = """
            SELECT f.film_id,
                   f.name,
                   f.description,
                   f.release_date,
                   f.duration,
                   r.rating_id,
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
            SELECT f.film_id,
                   f.name,
                   f.description,
                   f.release_date,
                   f.duration,
                   r.rating_id,
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


    private static final String FIND_POPULAR_QUERY = """
            SELECT f.film_id,
                   f.name,
                   f.description,
                   f.release_date,
                   f.duration,
                   r.rating_id,
                   r.name AS rating_name,
                   GROUP_CONCAT(DISTINCT (g.genre_id || ':' || g.name)) AS genre_data,
                   GROUP_CONCAT(DISTINCT l.user_id) AS likes,
                   COUNT(l.user_id) AS like_count
            FROM films f
            LEFT JOIN ratings r ON f.rating_id = r.rating_id
            LEFT JOIN film_genres fg ON f.film_id = fg.film_id
            LEFT JOIN genres g ON fg.genre_id = g.genre_id
            LEFT JOIN likes l ON f.film_id = l.film_id
            GROUP BY f.film_id
            HAVING COUNT(l.user_id) > 0
            ORDER BY like_count DESC, f.film_id DESC
            LIMIT ?
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

    private static final String INSERT_GENRE_QUERY = """
            INSERT INTO film_genres (film_id, genre_id)
            VALUES (?, ?)
            """;

    private static final String DELETE_LIKE_QUERY = """
            DELETE FROM likes
            WHERE film_id = ? AND user_id = ?
            """;

    private static final String INSERT_LIKE_QUERY = """
            INSERT INTO likes (film_id, user_id) VALUES (?, ?)
            """;


    @Autowired
    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<Film> findAll() {
        return findAll(FIND_ALL_QUERY);
    }


    @Override
    public Film findById(long id) {
        return findById(FIND_BY_ID_QUERY, id);
    }

    @Override
    public Film addFilm(Film film) {

        long id = -1;

        try {
            id = insert(
                    INSERT_QUERY,
                    film.getName(),
                    film.getDescription(),
                    Date.valueOf(film.getReleaseDate()),
                    film.getDuration().toMinutes(),
                    film.getMpa().getId());
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException("Rating with id " + film.getMpa().getId() + " not found");
        }

        film.setId(id);
        insertGenres(film);
        log.info("Film with id {} saved", id);
        return film;

    }

    @Override
    public Film updateFilm(Film film) {
        update(UPDATE_QUERY, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration().toMinutes(), film.getMpa().getId(), film.getId());
        log.info("Film with id {} updated", film.getId());
        return film;
    }

    @Override
    public void addLike(long filmId, long userId) {

        int rowsUpdated = jdbcTemplate.update(INSERT_LIKE_QUERY, filmId, userId);
        if (rowsUpdated == 0) {
            log.error("Cannot add like from user with id: {} to film with id: {}", userId, filmId);
        }

    }

    @Override
    public void removeLike(long filmId, long userId) {
        int rowsDeleted = jdbcTemplate.update(DELETE_LIKE_QUERY, filmId, userId);
        if (rowsDeleted == 0) {
            String errorMessage = "Cannot not delete like";
            log.error("Cannot not delete like from user with id: {} from film with id: {}", userId, filmId);
            throw new NotFoundException(errorMessage);
        }
    }

    private void insertGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) return;

        List<Object[]> batchArgs = film.getGenres().stream()
                .map(genre -> new Object[]{film.getId(), genre.getId()})
                .collect(Collectors.toList());

        try {
            jdbcTemplate.batchUpdate(INSERT_GENRE_QUERY, batchArgs);
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException("Genre not found");
        }
    }

    public List<Film> findPopular(int size) {
        return findAll(FIND_POPULAR_QUERY, size);
    }


}