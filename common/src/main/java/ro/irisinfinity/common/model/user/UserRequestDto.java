package ro.irisinfinity.common.model.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Date;

public record UserRequestDto(
        @Size(min = 5, max = 100)
        @NotNull(message = "Email is mandatory")
        String email,

        @Size(min = 5, max = 100)
        @NotNull(message = "Password is mandatory")
        String password,

        @Size(min = 2, max = 50)
        @NotNull(message = "First name is mandatory")
        String firstName,

        @Size(min = 2, max = 50)
        @NotNull(message = "Last name is mandatory")
        String lastName,

        @NotNull(message = "Birth date is mandatory")
        Date birthDate,

        @Size(min = 1, max = 1)
        @NotNull(message = "Sex is mandatory")
        String sex
) {}