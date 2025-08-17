package ro.irisinfinity.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.irisinfinity.auth.service.AuthService;
import ro.irisinfinity.platform.common.dto.auth.CredentialsRequestDto;
import ro.irisinfinity.platform.common.dto.user.UserRequestDto;
import ro.irisinfinity.platform.common.dto.user.UserResponseDto;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping
    public void auth(@RequestBody @Valid final CredentialsRequestDto credentialsRequestDto) {
        authService.auth(credentialsRequestDto);
    }

    @PostMapping("/register")
    public UserResponseDto registerUser(@RequestBody @Valid final UserRequestDto userRequestDto) {
        return authService.registerUser(userRequestDto);
    }
}