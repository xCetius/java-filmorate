package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.util.List;

@RequiredArgsConstructor
@Service
public class GenreService {

    private final GenreDbStorage genreDbStorage;

    public Genre getGenre(long id) {
        return genreDbStorage.findById(id);
    }

    public List<Genre> getGenres() {
        return genreDbStorage.findAll();
    }


}
