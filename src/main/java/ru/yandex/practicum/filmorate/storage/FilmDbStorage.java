package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;
    private final GenreDbStorage genreDbStorage;


    @Override
    public List<Film> getFilms() {
        String sql = """
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
        try {
            return jdbcTemplate.query(sql, filmRowMapper);
        } catch (EmptyResultDataAccessException e) {
            String errorMessage = "No films found";
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }

    }


    @Override
    public Film getFilm(long id) {
        Film film;

        String sql = """
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

        try {
            film = jdbcTemplate.queryForObject(sql, filmRowMapper, id);
        } catch (Exception e) {
            log.error("Cannot get film: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        if (film == null) {
            String errorMessage = "Film with id " + id + " not found";
            log.error("Could not find film: {}", errorMessage);
            throw new NotFoundException(errorMessage);
        }
        return film;
    }

    @Override
    public Film addFilm(Film film) {
        String insertSql = """
                    INSERT INTO films (name, description, release_date, duration, rating_id)
                    VALUES (?, ?, ?, ?, ?)
                """;

        //Проверка на наличие жанров в базе
        if (film.getGenres() != null) {
            List<Long> filmGenres = film.getGenres().stream().map(Genre::getId).toList();
            List<Long> possibleGenres = genreDbStorage.getGenres().stream().map(Genre::getId).toList();


            for (Long genreId : filmGenres) {
                if (!possibleGenres.contains(genreId)) {
                    String errorMessage = "Genre with id " + genreId + " not found";
                    log.error("Validation failed: {}", errorMessage);
                    throw new NotFoundException(errorMessage);
                }
            }
        }


        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setLong(4, film.getDuration().toMinutes());
            ps.setLong(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        long filmId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        film.setId(filmId);
        insertGenres(film);
        return getFilm(filmId);
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = """
                UPDATE films
                SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ?
                WHERE film_id = ?
                """;
        try {
            jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration().toMinutes(), film.getMpa().getId(), film.getId());
        } catch (Exception e) {
            String errorMessage = "Cannot update film: " + e.getMessage();
            log.error("Update failed: {}", errorMessage);
            throw new ValidationException(errorMessage);
        }
        return film;
    }

    @Override
    public void addLike(long filmId, long userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";

        try {
            jdbcTemplate.update(sql, filmId, userId);
        } catch (Exception e) {
            String errorMessage = "Cannot add like: " + e.getMessage();
            log.error("Add like failed: {}", errorMessage);
            throw new ValidationException(errorMessage);
        }
    }

    @Override
    public void removeLike(long filmId, long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

        try {
            jdbcTemplate.update(sql, filmId, userId);
        } catch (Exception e) {
            String errorMessage = "Cannot remove like: " + e.getMessage();
            log.error("Remove like failed: {}", errorMessage);
            throw new ValidationException(errorMessage);
        }

    }

    private void insertGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) return;

        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        List<Object[]> batchArgs = film.getGenres().stream()
                .map(genre -> new Object[]{film.getId(), genre.getId()})
                .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

}