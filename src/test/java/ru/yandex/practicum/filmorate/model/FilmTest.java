package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
class FilmTest {

    @Autowired
    @Qualifier("inMemoryFilmStorage")
    private FilmStorage filmStorage;
    @Autowired
    private FilmService filmService;


    @Test
    void testValidateFilmWithValidData() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("This is a test film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(120));

        // Проверка, что валидация проходит без ошибок
        assertDoesNotThrow(() -> filmService.validateFilm(film));
    }

    @Test
    void testValidateFilmWithLongDescription() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("This is a very long description that exceeds the limit of 200 characters. "
                + "This is a very long description that exceeds the limit of 200 characters. "
                + "This is a very long description that exceeds the limit of 200 characters.");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(120));

        // Проверка, что валидация выбрасывает исключение
        ValidationException exception = assertThrows(ValidationException.class, () -> filmService.validateFilm(film));
        assertEquals("Film description must be less than 200 symbols", exception.getMessage());
    }

    @Test
    void testValidateFilmWithNegativeDuration() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("This is a test film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(-100)); // Отрицательная длительность

        // Проверка, что валидация выбрасывает исключение
        ValidationException exception = assertThrows(ValidationException.class, () -> filmService.validateFilm(film));
        assertEquals("Film duration must be positive", exception.getMessage());
    }

    @Test
    void testValidateFilmWithZeroDuration() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("This is a test film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(0)); // Нулевая длительность

        // Проверка, что валидация выбрасывает исключение
        ValidationException exception = assertThrows(ValidationException.class, () -> filmService.validateFilm(film));
        assertEquals("Film duration must be positive", exception.getMessage());
    }

    @Test
    void testValidateFilmWithEarlyReleaseDate() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("This is a test film");
        film.setReleaseDate(LocalDate.of(1895, 12, 27)); // Дата до 1985-12-28
        film.setDuration(Duration.ofMinutes(120));

        // Проверка, что валидация выбрасывает исключение
        ValidationException exception = assertThrows(ValidationException.class, () -> filmService.validateFilm(film));
        assertEquals("Film release date must not be before 1895-12-28", exception.getMessage());
    }

    @Test
    void testValidateFilmWithBoundaryReleaseDate() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("This is a test film");
        film.setReleaseDate(LocalDate.of(1895, 12, 28)); // Граничная дата
        film.setDuration(Duration.ofMinutes(120));
        film.

        // Проверка, что валидация проходит без ошибок
        assertDoesNotThrow(() -> filmService.validateFilm(film));
    }

    @Test
    void testSettersAndGetters() {
        Film film = new Film();
        film.setId(1L);
        film.setName("Test Film");
        film.setDescription("This is a test film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(120));

        // Проверка сеттеров и геттеров
        assertEquals(1L, film.getId());
        assertEquals("Test Film", film.getName());
        assertEquals("This is a test film", film.getDescription());
        assertEquals(LocalDate.of(2000, 1, 1), film.getReleaseDate());
        assertEquals(Duration.ofMinutes(120), film.getDuration());
    }
}