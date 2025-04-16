package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.FriendshipStatus;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("inMemoryUserStorage")
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User findById(long id) {
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

        users.put(user.getId(), user);
        log.info("User with id {} updated", user.getId());
        return user;

    }

    @Override
    public void addFriend(User user, User friend) {
        long userId = user.getId();
        long friendId = friend.getId();
        user.getFriends().put(friendId, FriendshipStatus.PENDING);

        log.info("Friend with id {} added to user with id {}", friendId, userId);

    }

    @Override
    public void removeFriend(User user, User friend) {

        long userId = user.getId();
        long friendId = friend.getId();

        user.getFriends().remove(friendId);
        log.info("Friend with id {} removed from user with id {}", friendId, userId);

    }


    public long getNextUserId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }


}
