package ro.irisinfinity.platform.common.dto.auth;

import jakarta.validation.constraints.Email;

public record LoginRequest(
    @Email
    String email,
    String password) {

}