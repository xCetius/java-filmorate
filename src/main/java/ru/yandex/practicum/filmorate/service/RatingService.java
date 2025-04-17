package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.RatingStorage;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RatingService {

    private final RatingStorage ratingStorage;

    public Rating getRating(long id) {
        return ratingStorage.findById(id);
    }

    public List<Rating> getRatings() {
        return ratingStorage.findAll();
    }
}
