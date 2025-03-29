package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getFilm(long id) {
        if (!films.containsKey(id)) {
            String errorMessage = "Film with id " + id + " not found";
            log.error("Cannot get film: {}", errorMessage);
            throw new NotFoundException(errorMessage);
        }
        return films.get(id);
    }

    @Override
    public Film addFilm(Film film) {
        long id = getNextFilmId();
        film.setId(id);
        validateFilm(film);
        films.put(id, film);
        log.info("Film with id {} added", id);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (!films.containsKey(film.getId())) {
            String errorMessage = "Film with id " + film.getId() + " not found";
            log.error("Cannot update film: {}", errorMessage);
            throw new NotFoundException(errorMessage);
        }
        validateFilm(film);
        films.put(film.getId(), film);
        log.info("Film with id {} updated", film.getId());
        return film;
    }

    @Override
    public long getNextFilmId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public void validateFilm(Film film) {
        log.debug("Starting validation for film: {}", film.getName());

        if (film.getDescription().length() > 200) {
            String errorMessage = "Film description must be less than 200 symbols";
            log.error("Validation failed: {}", errorMessage);
            throw new ValidationException(errorMessage);
        }

        if (film.getDuration().getSeconds() <= 0) {
            String errorMessage = "Film duration must be positive";
            log.error("Validation failed: {}", errorMessage);
            throw new ValidationException(errorMessage);
        }

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            String errorMessage = "Film release date must not be before 1895-12-28";
            log.error("Validation failed: {}", errorMessage);
            throw new ValidationException(errorMessage);
        }

        log.info("Film validation successful: {}", film.getName());
    }
}
