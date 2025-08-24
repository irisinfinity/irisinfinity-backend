package ro.irisinfinity.auth.controller;

import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.irisinfinity.auth.service.AuthCoreService;
import ro.irisinfinity.platform.common.dto.auth.LoginRequest;
import ro.irisinfinity.platform.common.dto.auth.RefreshRequest;
import ro.irisinfinity.platform.common.dto.auth.TokenResponse;
import ro.irisinfinity.platform.common.dto.users.UserRequestDto;
import ro.irisinfinity.platform.common.dto.users.UserResponseDto;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthCoreService authCoreService;

    @PostMapping("/register")
    public UserResponseDto register(@RequestBody @Valid final UserRequestDto userRequestDto) {
        return authCoreService.register(userRequestDto);
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        return authCoreService.login(request.email(), request.password());
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody RefreshRequest request) {
        return authCoreService.refresh(request.refreshToken());
    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        return jwt.getClaims();
    }
}