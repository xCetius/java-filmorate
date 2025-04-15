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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

        if (user.getFriends().contains(friendId)) {
            log.info("User with id {} already has friend with id {}", userId, friendId);
            throw new ValidationException("User with id " + userId + " already has friend with id " + friendId);
        }

        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(long userId, long friendId) throws ValidationException {
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);

        validateUser(user);
        validateUser(friend);

        userStorage.removeFriend(userId, friendId);


    }

    public List<User> getFriends(long userId) throws ValidationException {
        User user = userStorage.findById(userId);
        validateUser(user);

        List<User> friends = new ArrayList<>(user.getFriends().size());
        for (Long friendId : user.getFriends()) {
            friends.add(userStorage.findById(friendId));
        }
        return friends;
    }

    public List<User> showCommonFriends(long userId, long friendId) throws ValidationException {
        List<User> commonFriends = new ArrayList<>();

        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);

        validateUser(user);
        validateUser(friend);

        Set<Long> userFriends = user.getFriends();
        Set<Long> friendFriends = friend.getFriends();

        if (userFriends.isEmpty() || friendFriends.isEmpty()) {
            log.info("User with id {} and friend with id {} have no common friends", userId, friendId);

        } else {
            userFriends.retainAll(friendFriends);
            log.info("User with id {} and friend with id {} have {} common friends", userId, friendId, userFriends.size());
            userFriends.stream().map(userStorage::findById).forEach(commonFriends::add);
        }
        return commonFriends;
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
