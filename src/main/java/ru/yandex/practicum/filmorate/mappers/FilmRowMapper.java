package ru.yandex.practicum.filmorate.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {

        Film film = new Film();

        try {
            film.setId(rs.getLong("film_id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(Duration.ofMinutes(rs.getLong("duration")));

            // MPA (Rating)
            Rating rating = new Rating();
            rating.setId(rs.getLong("rating_id"));
            rating.setName(rs.getString("rating_name"));
            film.setMpa(rating);

            // Likes
            Set<Long> likes = new HashSet<>();
            String likesConcat = rs.getString("likes");
            if (likesConcat != null && !likesConcat.isBlank()) {
                likes = Arrays.stream(likesConcat.split(","))
                        .map(String::trim)
                        .map(Long::parseLong)
                        .collect(Collectors.toSet());
            }
            film.setLikes(likes);

            // Genres
            Set<Genre> genres = new LinkedHashSet<>();
            String genreData = rs.getString("genre_data");
            if (genreData != null && !genreData.isBlank()) {
                genres = Arrays.stream(genreData.split(","))
                        .map(String::trim)
                        .map(pair -> {
                            String[] parts = pair.split(":");
                            Genre genre = new Genre();
                            genre.setId(Long.parseLong(parts[0]));
                            genre.setName(parts[1]);
                            return genre;
                        })
                        .sorted(Comparator.comparingLong(Genre::getId))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
            }
            film.setGenres(genres);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return film;
    }
}
