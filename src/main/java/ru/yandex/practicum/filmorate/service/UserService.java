package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }


    public void addFriend(long userId, long friendId) throws ValidationException {
        User user = userStorage.getUser(userId);
        User friend = userStorage.getUser(friendId);

        userStorage.validateUser(user);
        userStorage.validateUser(friend);

        if (user.getFriends().add(friendId)) {
            friend.getFriends().add(userId);
            log.info("Friend with id {} added to user with id {}", friendId, userId);
        } else {
            log.info("Friend with id {} already exists in user with id {}", friendId, userId);
        }


    }

    public void removeFriend(long userId, long friendId) throws ValidationException {
        User user = userStorage.getUser(userId);
        User friend = userStorage.getUser(friendId);

        userStorage.validateUser(user);
        userStorage.validateUser(friend);

        if (user.getFriends().remove(friendId)) {
            log.info("Friend with id {} removed from user with id {}", friendId, userId);
            friend.getFriends().remove(userId);
        } else {
            log.info("Friend with id {} not found in user with id {}", friendId, userId);
        }
    }

    public List<User> getFriends(long userId) throws ValidationException {
        User user = userStorage.getUser(userId);
        userStorage.validateUser(user);

        List<User> friends = new ArrayList<>(user.getFriends().size());
        for (Long friendId : user.getFriends()) {
            friends.add(userStorage.getUser(friendId));
        }
        return friends;
    }

    public List<User> showCommonFriends(long userId, long friendId) throws ValidationException {
        List<User> commonFriends = new ArrayList<>();

        User user = userStorage.getUser(userId);
        User friend = userStorage.getUser(friendId);

        userStorage.validateUser(user);
        userStorage.validateUser(friend);

        Set<Long> userFriends = user.getFriends();
        Set<Long> friendFriends = friend.getFriends();

        if (userFriends.isEmpty() || friendFriends.isEmpty()) {
            log.info("User with id {} and friend with id {} have no common friends", userId, friendId);

        } else {
            userFriends.retainAll(friendFriends);
            log.info("User with id {} and friend with id {} have {} common friends", userId, friendId, userFriends.size());
            userFriends.stream().map(userStorage::getUser).forEach(commonFriends::add);
        }
        return commonFriends;
    }


}
