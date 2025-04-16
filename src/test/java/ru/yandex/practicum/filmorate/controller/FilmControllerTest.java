package ru.yandex.practicum.filmorate.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Set;


import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@Sql({"/schema.sql", "/test-data.sql"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FilmControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final FilmService filmService;
    private final UserDbStorage userStorage;

    private Film validFilm;
    private User validUser;
    private static Rating rating;


    @BeforeAll
    static void beforeAll() {
        rating = new Rating();
        rating.setId(1L);
        rating.setName("G");
    }

    @BeforeEach
    void setUp() {
        validFilm = new Film();
        validFilm.setName("Test Film");
        validFilm.setDescription("This is a test film");
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        validFilm.setDuration(Duration.ofMinutes(120));
        validFilm.setMpa(rating);


        validUser = new User();
        validUser.setName("Test User");
        validUser.setLogin("testuser");
        validUser.setEmail("test@example.com");
        validUser.setBirthday(LocalDate.of(2000, 1, 1));
    }

    @Test
    void testAddFilm() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Film"))
                .andExpect(jsonPath("$.description").value("This is a test film"))
                .andExpect(jsonPath("$.releaseDate").value("2000-01-01"))
                .andExpect(jsonPath("$.duration").value(120))
                .andExpect(jsonPath("$.mpa.name").value("G"));
    }

    @Test
    void testAddFilmFailMpa() throws Exception {

        Rating invalidRating = new Rating();
        invalidRating.setId(55L);
        invalidRating.setName("M");

        Film invalidFilm = new Film();
        invalidFilm.setName("adsasd");
        invalidFilm.setDescription("This is a test film");
        invalidFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        invalidFilm.setDuration(Duration.ofMinutes(120));
        invalidFilm.setMpa(invalidRating);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAddFilmFailGenre() throws Exception {

        Genre invalidGenre = new Genre();
        invalidGenre.setId(55L);

        Set<Genre> genres = Set.of(invalidGenre);

        Film invalidFilm = new Film();
        invalidFilm.setName("adsasd");
        invalidFilm.setDescription("This is a test film");
        invalidFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        invalidFilm.setDuration(Duration.ofMinutes(120));
        invalidFilm.setMpa(rating);
        invalidFilm.setGenres(genres);


        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAddFilmWithGenres() throws Exception {

        Genre genre1 = new Genre();
        genre1.setId(1L);

        Genre genre2 = new Genre();
        genre2.setId(2L);

        Set<Genre> genres = Set.of(genre1, genre2);
        validFilm.setGenres(genres);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Film"))
                .andExpect(jsonPath("$.description").value("This is a test film"))
                .andExpect(jsonPath("$.releaseDate").value("2000-01-01"))
                .andExpect(jsonPath("$.duration").value(120))
                .andExpect(jsonPath("$.genres.length()").value(2));
    }

    @Test
    void testUpdateFilm() throws Exception {
        // Сначала добавляем фильм
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isOk());

        // Обновляем фильм
        validFilm.setName("Updated Film");
        validFilm.setId(1L);
        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Film"));
    }

    @Test
    void testUpdateNonExistentFilm() throws Exception {
        Film nonExistentFilm = new Film();
        nonExistentFilm.setId(999L);
        nonExistentFilm.setName("Non-existent Film");
        nonExistentFilm.setDescription("This film does not exist");
        nonExistentFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        nonExistentFilm.setDuration(Duration.ofMinutes(120));
        nonExistentFilm.setMpa(rating);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nonExistentFilm)))
                .andExpect(status().isNotFound());

    }

    @Test
    void testGetFilms() throws Exception {
        // Добавляем фильм
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isOk());

        // Получаем список фильмов
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Film"));
    }

    @Test
    void testAddFilmWithInvalidName() throws Exception {
        Film invalidFilm = new Film();
        invalidFilm.setName("");
        invalidFilm.setDescription("This is a test film");
        invalidFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        invalidFilm.setDuration(Duration.ofMinutes(120));
        validFilm.setMpa(rating);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddLike() throws Exception {
        userStorage.addUser(validUser);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isOk());

        mockMvc.perform(put("/films/1/like/1"))
                .andExpect(status().isOk());

    }

    @Test
    void testAddLikeWithInvalidFilmId() throws Exception {
        mockMvc.perform(put("/films/999/like/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAddLikeWithInvalidUserId() throws Exception {
        mockMvc.perform(put("/films/1/like/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testRemoveLike() throws Exception {
        userStorage.addUser(validUser);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isOk());

        mockMvc.perform(put("/films/1/like/1"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/films/1/like/1"))
                .andExpect(status().isOk());

    }

    @Test
    void testRemoveLikeWithNoLike() throws Exception {
        userStorage.addUser(validUser);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/films/1/like/1"))
                .andExpect(status().isNotFound());

    }

    @Test
    void testReturnTopFilms() throws Exception {
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk());
    }

    @Test
    void getPopularFilms_WhenSizeIsZero_ShouldThrowValidationException() {
        // Проверяем, что при size <= 0 выбрасывается исключение
        assertThrows(ValidationException.class, () -> filmService.getPopularFilms(0));
        assertThrows(ValidationException.class, () -> filmService.getPopularFilms(-1));
    }

    @Test
    void getPopularFilms_ShouldReturnFilms() throws Exception {
        int size = 5;
        userStorage.addUser(validUser);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isOk());

        mockMvc.perform(put("/films/1/like/1"))
                .andExpect(status().isOk());


        mockMvc.perform(get("/films/popular").param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/films/popular").param("size", String.valueOf(-1)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

    }
}