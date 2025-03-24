package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import ru.yandex.practicum.filmorate.model.User;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        long id = getNextUserId();
        user.setId(id);
        user.validateUser();
        users.put(id, user);
        log.info("User with id {} added", id);
        return user;
    }

    @PutMapping()
    public User updateUser(@Valid @RequestBody User user) {
        if (!users.containsKey(user.getId())) {
            String errorMessage = "User with id " + user.getId() + " not found";
            log.error("Cannot update user: {}", errorMessage);
            throw new NotFoundException(errorMessage);
        }
        user.validateUser();
        users.put(user.getId(), user);
        log.info("User with id {} updated", user.getId());
        return user;
    }

    private long getNextUserId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }


}

