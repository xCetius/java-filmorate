package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.RatingStorage;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class RatingController {

    private final RatingStorage ratingStorage;

    @GetMapping
    public List<Rating> getRatings() {
        return ratingStorage.getRatings();
    }

    @GetMapping("/{id}")
    public Rating getRating(@PathVariable("id") long id) {
        return ratingStorage.getRating(id);
    }
}
