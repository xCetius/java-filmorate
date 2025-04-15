package ru.yandex.practicum.filmorate.storage;


import ru.yandex.practicum.filmorate.model.User;

import java.util.List;


public interface UserStorage {

    List<User> findAll();

    User findById(long id);

    User addUser(User user);

    User updateUser(User user);

    void addFriend(User userId, User friend);

    void removeFriend(User userId, User friend);


}
