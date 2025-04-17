package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository("inMemoryFilmStorage")
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film findById(long id) {
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

        films.put(film.getId(), film);
        log.info("Film with id {} updated", film.getId());
        return film;
    }

    @Override
    public void addLike(long filmId, long userId) {
        Film film = findById(filmId);

        if (film.getLikes().add(userId)) {
            log.info("Film with id {} liked by user with id {}", filmId, userId);
        } else {
            log.info("Film with id {} already liked by user with id {}", filmId, userId);
            throw new ValidationException("Film with id " + filmId + " already liked by user with id " + userId);
        }
    }

    @Override
    public void removeLike(long filmId, long userId) {
        Film film = findById(filmId);
        if (film.getLikes().remove(userId)) {
            log.info("Film with id {} unliked by user with id {}", filmId, userId);
        } else {
            log.info("Cannot remove like from film with id {}. Film was not liked by user with id {}", filmId, userId);
            throw new NotFoundException("Film was not liked by user with id " + userId);
        }
    }

    public long getNextFilmId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public List<Film> findPopular(int size) {
        List<Film> allFilms = findAll();

        List<Film> sortedFilms = allFilms.stream()
                .filter(film -> !film.getLikes().isEmpty())
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).thenComparing(Film::getId).reversed())
                .toList();

        return sortedFilms.stream()
                .limit(Math.min(size, sortedFilms.size()))
                .collect(Collectors.toList());
    }




}
