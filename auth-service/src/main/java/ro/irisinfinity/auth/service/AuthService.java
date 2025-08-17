package ro.irisinfinity.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ro.irisinfinity.auth.client.UsersClient;
import ro.irisinfinity.platform.common.dto.auth.CredentialsRequestDto;
import ro.irisinfinity.platform.common.dto.auth.CredentialsResponseDto;
import ro.irisinfinity.platform.common.dto.auth.EmailLookupRequestDto;
import ro.irisinfinity.platform.common.dto.user.UserRequestDto;
import ro.irisinfinity.platform.common.dto.user.UserResponseDto;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsersClient usersClient;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public void auth(final CredentialsRequestDto credentialsRequestDto) {
        EmailLookupRequestDto emailLookupRequestDto = objectMapper.convertValue(
            credentialsRequestDto, EmailLookupRequestDto.class);

        CredentialsResponseDto credentialsResponseDto = usersClient.findCredentials(
            emailLookupRequestDto);
    }

    public UserResponseDto registerUser(final UserRequestDto userRequestDto) {
        String password = userRequestDto.password();
        String passwordHash = passwordEncoder.encode(password);
        UserRequestDto sanitizedUserRequestDto = userRequestDto.withPassword(passwordHash);

        return usersClient.createUser(sanitizedUserRequestDto);
    }
}