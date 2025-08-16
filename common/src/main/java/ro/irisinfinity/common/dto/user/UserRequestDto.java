package ro.irisinfinity.common.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import ro.irisinfinity.common.enums.Sex;

public record UserRequestDto(

    @NotNull(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    @Size(min = 5, max = 100, message = "Email must be between 5 and 100 characters")
    String email,

    @NotNull(message = "Password is mandatory")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[(.!?@#$%^&*+=_)]).+$",
        message = "Password must contain uppercase, lowercase, number, and special character (.!?@#$%^&*+=_)"
    )
    String password,

    @NotNull(message = "First name is mandatory")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    String firstName,

    @NotNull(message = "Last name is mandatory")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    String lastName,

    @NotNull(message = "Birth date is mandatory")
    @Past(message = "Birth date must be in the past")
    LocalDate birthDate,

    @NotNull(message = "Sex is mandatory")
    Sex sex
) {

}
