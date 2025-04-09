package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;


    public void addFriend(long userId, long friendId) throws ValidationException {
        User user = userStorage.getUser(userId);
        User friend = userStorage.getUser(friendId);

        validateUser(user);
        validateUser(friend);

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

        validateUser(user);
        validateUser(friend);

        if (user.getFriends().remove(friendId)) {
            log.info("Friend with id {} removed from user with id {}", friendId, userId);
            friend.getFriends().remove(userId);
        } else {
            log.info("Friend with id {} not found in user with id {}", friendId, userId);
        }
    }

    public List<User> getFriends(long userId) throws ValidationException {
        User user = userStorage.getUser(userId);
        validateUser(user);

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

        validateUser(user);
        validateUser(friend);

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
