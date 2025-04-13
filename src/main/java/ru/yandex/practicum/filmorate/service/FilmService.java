package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.RatingStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;


import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final RatingStorage ratingStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, @Qualifier("UserDbStorage") UserStorage userStorage, RatingStorage ratingStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.ratingStorage = ratingStorage;
    }

    public void addLike(long filmId, long userId) throws ValidationException {
        Film film = filmStorage.getFilm(filmId);
        User user = userStorage.getUser(userId);
        if (user == null) {
            log.error("User with id {} not found", userId);
            throw new NotFoundException("User with id " + userId + " not found");
        }
        if (film == null) {
            log.error("Film with id {} not found", filmId);
            throw new NotFoundException("Film with id " + filmId + " not found");
        }

        filmStorage.addLike(filmId, userId);
        log.info("Film with id {} liked by user with id {}", filmId, userId);

    }

    public void removeLike(long filmId, long userId) throws ValidationException {
        Film film = filmStorage.getFilm(filmId);
        User user = userStorage.getUser(userId);
        if (user == null) {
            log.error("User with id {} not found", userId);
            throw new NotFoundException("User with id " + userId + " not found");
        }
        if (film == null) {
            log.error("Film with id {} not found", filmId);
            throw new NotFoundException("Film with id " + filmId + " not found");
        }

        filmStorage.removeLike(filmId, userId);
        log.info("Film with id {} unliked by user with id {}", filmId, userId);
    }

    public List<Film> getPopularFilms(int size) {
        if (size <= 0) {
            log.error("Requested popular films size must be positive, but was: {}", size);
            throw new ValidationException("Size must be greater than 0");
        }

        List<Film> allFilms = filmStorage.getFilms();

        List<Film> sortedFilms = allFilms.stream()
                .filter(film -> !film.getLikes().isEmpty())
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).thenComparing(Film::getId).reversed())
                .toList();

        return sortedFilms.stream()
                .limit(Math.min(size, sortedFilms.size()))
                .collect(Collectors.toList());
    }

    public void validateFilm(Film film) {
        log.debug("Starting validation for film: {}", film.getName());

        if (film.getName().length() > 50) {
            String errorMessage = "Film name must be less than 50 symbols";
            log.error("Validation failed: {}", errorMessage);
            throw new ValidationException(errorMessage);
        }

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

        //Проверка на наличие рейтинга в базе
        List<Long> possibleRatings = ratingStorage.getRatings().stream().map(Rating::getId).toList();
        Long ratingId = film.getMpa().getId();

        if (!possibleRatings.contains(ratingId)) {
            String errorMessage = "Rating with id " + film.getMpa().getId() + " not found";
            log.error("Validation failed: {}", errorMessage);
            throw new NotFoundException(errorMessage);
        }


        log.info("Film validation successful: {}", film.getName());
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilm(long id) {
        return filmStorage.getFilm(id);
    }

    public Film addFilm(Film film) {
        validateFilm(film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        validateFilm(film);
        if (getFilm(film.getId()) == null) {
            String errorMessage = "Film with id " + film.getId() + " not found";
            log.error("Cannot update film: {}", errorMessage);
            throw new NotFoundException(errorMessage);
        }
        return filmStorage.updateFilm(film);
    }


}
