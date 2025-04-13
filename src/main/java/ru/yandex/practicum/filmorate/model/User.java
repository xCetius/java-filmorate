package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;



import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


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
    private Set<Long> friends = new HashSet<>();


}
