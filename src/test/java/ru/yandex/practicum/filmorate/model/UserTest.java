package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserTest {

    @Autowired
    private UserStorage userStorage;

    @Autowired
    private UserService userService;

    @Test
    void testValidateUserWithValidData() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testLogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        // Проверка, что валидация проходит без ошибок
        assertDoesNotThrow(() -> userService.validateUser(user));

        // Проверка, что имя заменяется логином, если оно пустое
        assertEquals("testLogin", user.getName());
    }

    @Test
    void testValidateUserWithEmptyName() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testLogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        // Имя не задано, должно быть заменено логином
        userService.validateUser(user);
        assertEquals("testLogin", user.getName());
    }

    @Test
    void testValidateUserWithFutureBirthday() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testLogin");
        user.setBirthday(LocalDate.now().plusDays(1)); // Дата рождения в будущем

        // Проверка, что валидация выбрасывает исключение
        ValidationException exception = assertThrows(ValidationException.class, () -> userService.validateUser(user));
        assertEquals("User birthday must be before current date", exception.getMessage());
    }


    @Test
    void testValidateUserWithNullBirthday() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testLogin");


        assertThrows(NullPointerException.class, () -> userService.validateUser(user));
    }

    @Test
    void testSettersAndGetters() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setLogin("testLogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        // Проверка сеттеров и геттеров
        assertEquals(1L, user.getId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("testLogin", user.getLogin());
        assertEquals("Test User", user.getName());
        assertEquals(LocalDate.of(2000, 1, 1), user.getBirthday());
    }
}