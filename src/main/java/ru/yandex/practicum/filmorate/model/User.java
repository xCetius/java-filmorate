package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.enums.FriendshipStatus;


import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;


@Data
public class User {
    private long id;
    private String name;
    @Email
    private String email;
    @NotNull
    @NotBlank
    @NotEmpty
    private String login;
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
    private Map<Long, FriendshipStatus> friends = new HashMap<>();


}
