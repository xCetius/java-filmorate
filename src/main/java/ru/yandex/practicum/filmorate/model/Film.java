package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.adapter.DurationToMinutesSerializer;
import ru.yandex.practicum.filmorate.adapter.MinutesToDurationDeserializer;
import ru.yandex.practicum.filmorate.exception.ValidationException;


import java.time.Duration;
import java.time.LocalDate;

@Slf4j
@Data
public class Film {
    private long id;
    @NotNull
    @NotBlank
    @NotEmpty
    private String name;
    private String description;
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;
    @NotNull
    @JsonSerialize(using = DurationToMinutesSerializer.class)
    @JsonDeserialize(using = MinutesToDurationDeserializer.class)
    private Duration duration;

    public void validateFilm() {
        log.debug("Starting validation for film: {}", this.name);

        if (this.description.length() > 200) {
            String errorMessage = "Film description must be less than 200 symbols";
            log.error("Validation failed: {}", errorMessage);
            throw new ValidationException(errorMessage);
        }

        if (this.duration.getSeconds() <= 0) {
            String errorMessage = "Film duration must be positive";
            log.error("Validation failed: {}", errorMessage);
            throw new ValidationException(errorMessage);
        }

        if (this.releaseDate.isBefore(LocalDate.of(1895, 12, 28))) {
            String errorMessage = "Film release date must not be before 1895-12-28";
            log.error("Validation failed: {}", errorMessage);
            throw new ValidationException(errorMessage);
        }

        log.info("Film validation successful: {}", this.name);
    }

}
