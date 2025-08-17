package ro.irisinfinity.platform.common.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CredentialsRequestDto(
    @Email
    String email,

    @NotBlank
    String password
) {

}
