package ru.yandex.practicum.filmorate.controller;


import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import ru.yandex.practicum.filmorate.model.Film;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {


    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        long id = getNextFilmId();
        film.setId(id);
        film.validateFilm();
        films.put(id, film);
        log.info("Film with id {} added", id);
        return film;
    }

    @PutMapping()
    public Film updateFilm(@Valid @RequestBody Film film) {
        if (!films.containsKey(film.getId())) {
            String errorMessage = "Film with id " + film.getId() + " not found";
            log.error("Cannot update film: {}", errorMessage);
            throw new NotFoundException(errorMessage);
        }
        film.validateFilm();
        films.put(film.getId(), film);
        log.info("Film with id {} updated", film.getId());
        return film;
    }

    private long getNextFilmId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }


}
