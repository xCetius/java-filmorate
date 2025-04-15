package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.FriendshipStatus;

import java.util.List;

@Repository("UserDbStorage")
@Slf4j
public class UserDbStorage extends BaseRepository<User> implements UserStorage {

    private static final String FIND_ALL_QUERY = """
            SELECT u.*,
            GROUP_CONCAT(f.friend_id) AS friends,
            FROM users u
            LEFT JOIN friends f ON u.user_id = f.user_id
            GROUP BY u.user_id
            """;

    private static final String FIND_BY_ID_QUERY = """
            SELECT u.*,
            GROUP_CONCAT(f.friend_id) AS friends,
            FROM users u
            LEFT JOIN friends f ON u.user_id = f.user_id
            WHERE u.user_id = ?
            GROUP BY u.user_id
            """;

    private static final String INSERT_QUERY = """
            INSERT INTO users (email, login, name, birthday)
            VALUES (?, ?, ?, ?)
            """;

    private static final String UPDATE_QUERY = """
            UPDATE users
            SET email = ?, login = ?, name = ?, birthday = ?
            WHERE user_id = ?
            """;

    private static final String USER_FRIENDS_QUERY = """
             SELECT u.*,
            GROUP_CONCAT(f.friend_id) AS friends,
            FROM users u
            JOIN friends f ON u.user_id = f.user_id
            WHERE u.user_id IN (SELECT friend_id FROM friends WHERE user_id = ?)
            GROUP BY u.user_id
            """;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }


    @Override
    public List<User> findAll() {
        try {
            return findAll(FIND_ALL_QUERY);
        } catch (EmptyResultDataAccessException e) {
            String errorMessage = "Users not found";
            log.error(e.getMessage());
            throw new NotFoundException(errorMessage);
        }
    }

    @Override
    public User findById(long id) {
        try {
            return findById(FIND_BY_ID_QUERY, id);
        } catch (EmptyResultDataAccessException e) {
            String errorMessage = "User with id " + id + " not found";
            log.error(e.getMessage());
            throw new NotFoundException(errorMessage);
        }
    }


    @Override
    public User addUser(User user) {

        long id = insert(
                INSERT_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday());

        user.setId(id);
        return user;
    }

    @Override
    public User updateUser(User user) {
        try {
            update(UPDATE_QUERY, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        } catch (Exception e) {
            String errorMessage = "Cannot update user: " + e.getMessage();
            log.error("Update failed: {} ", errorMessage);
            throw new ValidationException(errorMessage);
        }
        return user;
    }

    @Override
    public void addFriend(long userId, long friendId) {

        boolean reverseFriendshipExists = existsFriendship(friendId, userId);

        String insertSql = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, ?)";
        if (reverseFriendshipExists) {
            // Если обратная связь есть, устанавливаем статус CONFIRMED для обоих записей
            jdbcTemplate.update(insertSql, userId, friendId, FriendshipStatus.CONFIRMED.name());
            String updateSql = "UPDATE friends SET status = ? WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
            jdbcTemplate.update(updateSql, FriendshipStatus.CONFIRMED.name(), userId, friendId, friendId, userId);
        } else {
            // Если обратной связи нет, добавляем новую запись со статусом PENDING
            jdbcTemplate.update(insertSql, userId, friendId, FriendshipStatus.PENDING.name());
        }


    }

    @Override
    public void removeFriend(long userId, long friendId) {

        boolean reverseFriendshipExists = existsFriendship(friendId, userId);

        if (reverseFriendshipExists) {
            // Если обратная связь есть, устанавливаем статус PENDING для друга до удаления из друзей
            String updateSql = "UPDATE friends SET status = ? WHERE friend_id = ? AND user_id = ?";
            jdbcTemplate.update(updateSql, FriendshipStatus.PENDING.name(), userId, friendId);
        }

        String deleteSql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(deleteSql, userId, friendId);
    }

    private boolean existsFriendship(long userId, long friendId) {
        String sql = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, userId, friendId);
        return count > 0;
    }


    public List<User> getFriends(long userId) {
        try {
            return jdbcTemplate.query(USER_FRIENDS_QUERY, mapper);
        } catch (EmptyResultDataAccessException e) {
            String errorMessage = "No friends for user with id " + userId + " found";
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
    }

}
