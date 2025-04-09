package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
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
    public long getNextFilmId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }


}
