package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;

@Slf4j
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

    public void validateUser() throws ValidationException {
        if (this.name == null || this.name.isEmpty() || this.name.isBlank()) {
            this.name = this.login;
            log.info("User name not provided, using login as name: {}", this.name);
        }

        if (this.birthday.isAfter(LocalDate.now())) {
            String errorMessage = "User birthday must be before current date";
            log.error("Validation failed: {}", errorMessage);
            throw new ValidationException(errorMessage);
        }
    }
}
