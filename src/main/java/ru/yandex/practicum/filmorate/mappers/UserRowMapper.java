package ru.yandex.practicum.filmorate.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.FriendshipStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());

        String friendsConcat = rs.getString("friends");
        Map<Long, FriendshipStatus> friends = new HashMap<>();

        if (friendsConcat != null && !friendsConcat.isBlank()) {
            friends = Arrays.stream(friendsConcat.split(","))
                    .map(String::trim)
                    .map(entry -> entry.split(":"))
                    .filter(parts -> parts.length == 2)
                    .collect(Collectors.toMap(
                            parts -> Long.parseLong(parts[0]),
                            parts -> FriendshipStatus.valueOf(parts[1])
                    ));
        }

        user.setFriends(friends);

        return user;
    }
}

