package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

@Component
public interface FilmStorage {

    List<Film> getFilms();

    Film getFilm(long id);

    Film addFilm(Film film);

    Film updateFilm(Film film);

    long getNextFilmId();

    void validateFilm(Film film);
}
