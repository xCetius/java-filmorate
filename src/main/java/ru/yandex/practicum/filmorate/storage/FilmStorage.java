package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    List<Film> getFilms();

    Film getFilm(long id);

    Film addFilm(Film film);

    Film updateFilm(Film film);

    long getNextFilmId();

}
