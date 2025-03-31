package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUser(long id) {
        if (!users.containsKey(id)) {
            String errorMessage = "User with id " + id + " not found";
            log.error("Cannot get user: {}", errorMessage);
            throw new NotFoundException(errorMessage);
        }
        return users.get(id);
    }

    @Override
    public User addUser(User user) {
        long id = getNextUserId();
        user.setId(id);
        validateUser(user);
        users.put(id, user);
        log.info("User with id {} added", id);
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (!users.containsKey(user.getId())) {
            String errorMessage = "User with id " + user.getId() + " not found";
            log.error("Cannot update user: {}", errorMessage);
            throw new NotFoundException(errorMessage);
        }
        validateUser(user);
        users.put(user.getId(), user);
        log.info("User with id {} updated", user.getId());
        return user;

    }

    @Override
    public long getNextUserId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @Override
    public void validateUser(User user) throws ValidationException {
        String userName = user.getName();

        if (userName == null || userName.isEmpty() || userName.isBlank()) {
            userName = user.getLogin();
            user.setName(userName);
            log.info("User name not provided, using login as name: {}", userName);
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            String errorMessage = "User birthday must be before current date";
            log.error("Validation failed: {}", errorMessage);
            throw new ValidationException(errorMessage);
        }
    }
}
