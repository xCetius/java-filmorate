package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;


import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public void addLike(long filmId, long userId) throws ValidationException {
        Film film = filmStorage.getFilm(filmId);
        User user = userStorage.getUser(userId);
        if (user == null) {
            log.error("User with id {} not found", userId);
            throw new NotFoundException("User with id " + userId + " not found");
        }
        if (film.getLikes().add(userId)) {
            log.info("Film with id {} liked by user with id {}", filmId, userId);
        } else {
            log.info("Film with id {} already liked by user with id {}", filmId, userId);
            throw new ValidationException("Film with id " + filmId + " already liked by user with id " + userId);
        }
    }

    public void removeLike(long filmId, long userId) throws ValidationException {
        Film film = filmStorage.getFilm(filmId);
        userStorage.getUser(userId);
        if (film.getLikes().remove(userId)) {
            log.info("Film with id {} unliked by user with id {}", filmId, userId);
        } else {
            log.info("Cannot remove like from film with id {}. Film was not liked by user with id {}", filmId, userId);
            throw new NotFoundException("Film was not liked by user with id " + userId);
        }
    }

    public List<Film> getPopularFilms(int size) {

        if (size <= 0) {
            log.error("Size must be greater than 0");
            throw new ValidationException("Size must be greater than 0");
        }

        Set<Film> films = new TreeSet<>((o1, o2) -> o2.getLikes().size() - o1.getLikes().size());
        List<Film> popularFilms;
        films.addAll(filmStorage.getFilms());
        if (films.size() > size) {
            popularFilms = films.stream().limit(size).toList();
        } else {
            popularFilms = films.stream().toList();
        }

        return popularFilms;

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
