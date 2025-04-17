package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class UserService {

    @Autowired
    public UserService(@Qualifier("UserDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    private final UserStorage userStorage;

    public List<User> getUsers() {
        return userStorage.findAll();
    }

    public User getUser(long id) {
        return userStorage.findById(id);
    }

    public User addUser(User user) {
        validateUser(user);
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        validateUser(user);
        if (userStorage.findById(user.getId()) == null) {
            log.info("User with id {} not found", user.getId());
            throw new NotFoundException("User with id " + user.getId() + " not found");
        }
        return userStorage.updateUser(user);
    }


    public void addFriend(long userId, long friendId) throws ValidationException {
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);

        validateUser(user);
        validateUser(friend);

        if (user.getFriends().containsKey(friendId)) {
            log.info("User with id {} already has friend with id {}", userId, friendId);
            throw new ValidationException("User with id " + userId + " already has friend with id " + friendId);
        }

        userStorage.addFriend(user, friend);
    }

    public void removeFriend(long userId, long friendId) throws ValidationException {
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);

        validateUser(user);
        validateUser(friend);

        userStorage.removeFriend(user, friend);


    }

    public List<User> getFriends(long userId) throws ValidationException {

        return userStorage.findFriendsByUserId(userId);
    }

    public List<User> showCommonFriends(long userId, long friendId) throws ValidationException {
        return userStorage.findCommonFriends(userId, friendId);
    }


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
