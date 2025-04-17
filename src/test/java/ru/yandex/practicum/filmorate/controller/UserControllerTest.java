package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {

    private User validUser;
    private User validFriend;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final UserService userService;

    @BeforeEach
    void setUp() {
        validUser = new User();
        validUser.setName("Test User");
        validUser.setLogin("testUser");
        validUser.setEmail("test@example.com");
        validUser.setBirthday(LocalDate.of(2000, 1, 1));

        validFriend = new User();
        validFriend.setName("Test User");
        validFriend.setLogin("testUser2");
        validFriend.setEmail("test2@example.com");
        validFriend.setBirthday(LocalDate.of(2000, 1, 1));

    }

    @Test
    void testAddUserWithValidData() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void testAddUserWithInvalidEmail() throws Exception {
        User user = new User();
        user.setEmail("invalid-email"); // Некорректный email
        user.setLogin("testLogin1");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddUserWithBlankLogin() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("   "); // Пустой логин
        user.setBirthday(LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddUserWithFutureBirthday() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testLogin2");
        user.setBirthday(LocalDate.now().plusDays(1)); // Дата в будущем

        Assertions.assertThrows(ValidationException.class, () -> userService.validateUser(user));

    }

    @Test
    void testAddUserWithTodayBirthday() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testLogin3");
        user.setBirthday(LocalDate.now());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("testLogin3"));

    }

    @Test
    void testAddUserWithNullBirthday() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testLogin4");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateUserWithInvalidEmail() throws Exception {
        User user = new User();
        user.setEmail("invalid-email"); // Некорректный email
        user.setLogin("testLogin5");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateUserWithNullName() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testLogin6");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("testLogin6"));
    }

    @DisplayName("Add 2 users and make them friends")
    @Test
    void addFriend_ShouldReturnOkAndCheckStatus() throws Exception {
        // Создание двух пользователей
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFriend)))
                .andExpect(status().isOk());

        // Юзер 1 добавляет Юзера 2
        mockMvc.perform(put("/users/{id}/friends/{friendId}", 1, 2))
                .andExpect(status().isOk());

        // Проверка, что у Юзера 1 друг есть и статус PENDING
        mockMvc.perform(get("/users/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.friends['2']").value("PENDING")); // 2-й пользователь считает 1-го PENDING

        // Юзер 2 добавляет Юзера 1
        mockMvc.perform(put("/users/{id}/friends/{friendId}", 2, 1))
                .andExpect(status().isOk());

        // Проверка, что теперь у обоих статус CONFIRMED
        mockMvc.perform(get("/users/{id}/friends", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].friends['1']").value("CONFIRMED"));

        mockMvc.perform(get("/users/{id}/friends", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].friends['2']").value("CONFIRMED"));
    }


    @Test
    void removeFriend_ShouldReturnOk() throws Exception {

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isOk());


        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFriend)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/users/{id}/friends/{friendId}", 1, 2))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/{id}/friends", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/users/{id}/friends", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void showCommonFriends_ShouldReturnOk() throws Exception {

        User validFriend2 = new User();

        validFriend2.setName("Test User");
        validFriend2.setLogin("testUser3");
        validFriend2.setEmail("test3@example.com");
        validFriend2.setBirthday(LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isOk());


        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFriend)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFriend2)))
                .andExpect(status().isOk());


        mockMvc.perform(put("/users/{id}/friends/{friendId}", 1, 3))
                .andExpect(status().isOk());

        mockMvc.perform(put("/users/{id}/friends/{friendId}", 2, 3))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/{id}/friends/common/{otherId}", 1, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(3));

    }


}