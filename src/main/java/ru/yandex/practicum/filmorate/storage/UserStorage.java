package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    List<User> getUsers();

    User getUser(long id);

    User addUser(User user);

    User updateUser(User user);

    long getNextUserId();

    void validateUser(User user);
}
