package ro.irisinfinity.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ro.irisinfinity.auth.client.UsersClient;
import ro.irisinfinity.platform.common.dto.user.UserRequestDto;
import ro.irisinfinity.platform.common.dto.user.UserResponseDto;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsersClient usersClient;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDto registerUser(UserRequestDto userRequestDto) {
        String password = userRequestDto.password();
        String passwordHash = passwordEncoder.encode(password);
        UserRequestDto sanitizedUserRequestDto = userRequestDto.withPassword(passwordHash);

        return usersClient.createUser(sanitizedUserRequestDto);
    }
}