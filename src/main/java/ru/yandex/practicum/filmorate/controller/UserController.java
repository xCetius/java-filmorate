package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @GetMapping
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable("id") long id) {
        return userService.getUser(id);
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        userService.validateUser(user);
        return userService.addUser(user);
    }

    @PutMapping()
    public User updateUser(@Valid @RequestBody User user) {
        userService.validateUser(user);
        return userService.updateUser(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable("id") long id, @PathVariable("friendId") long friendId) {
        userService.addFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable("id") long id) {
        return userService.getFriends(id);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable("id") long id, @PathVariable("friendId") long friendId) {
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> showCommonFriends(@PathVariable("id") long id, @PathVariable("otherId") long otherId) {
        return userService.showCommonFriends(id, otherId);
    }


}

