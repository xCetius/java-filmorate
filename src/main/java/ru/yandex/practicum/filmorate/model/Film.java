package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.yandex.practicum.filmorate.adapter.DurationToMinutesSerializer;
import ru.yandex.practicum.filmorate.adapter.MinutesToDurationDeserializer;


import java.time.Duration;
import java.time.LocalDate;
import java.util.Set;


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
    @NotNull
    private Duration duration;
    private Set<Long> likes;
    @NotNull
    private Rating mpa;
    private Set<Genre> genres;

}
