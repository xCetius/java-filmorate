package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.FriendshipStatus;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Repository("UserDbStorage")
@RequiredArgsConstructor
@Slf4j
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;


    @Override
    public List<User> getUsers() {
        String sql = """
                SELECT u.*,
                GROUP_CONCAT(f.friend_id) AS friends,
                FROM users u
                LEFT JOIN friends f ON u.user_id = f.user_id
                GROUP BY u.user_id
                """;
        try {
            return jdbcTemplate.query(sql, userRowMapper);
        } catch (EmptyResultDataAccessException e) {
            String errorMessage = "Users not found";
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
    }

    @Override
    public User getUser(long id) {
        String sql = """
                SELECT u.*,
                GROUP_CONCAT(f.friend_id) AS friends,
                FROM users u
                LEFT JOIN friends f ON u.user_id = f.user_id
                WHERE u.user_id = ?
                GROUP BY u.user_id
                """;
        try {
            return jdbcTemplate.queryForObject(sql, userRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            String errorMessage = "User with id " + id + " not found";
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
    }


    @Override
    public User addUser(User user) {
        String sql = """
                INSERT INTO users (email, login, name, birthday)
                VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        user.setId(id);
        return user;
    }

    @Override
    public User updateUser(User user) {
        String sql = """
                UPDATE users
                SET email = ?, login = ?, name = ?, birthday = ?
                WHERE user_id = ?
                """;
        try {
            jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
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

    public boolean existsFriendship(long userId, long friendId) {
        String sql = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, userId, friendId);
        return count > 0;
    }


    public List<User> getFriends(long userId) {
        String sql = """
                 SELECT u.*,
                GROUP_CONCAT(f.friend_id) AS friends,
                FROM users u
                JOIN friends f ON u.user_id = f.user_id
                WHERE u.user_id IN (SELECT friend_id FROM friends WHERE user_id = ?)
                GROUP BY u.user_id
                """;
        try {
            return jdbcTemplate.query(sql, userRowMapper);
        } catch (EmptyResultDataAccessException e) {
            String errorMessage = "No friends for user with id " + userId + " found";
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
    }

}
