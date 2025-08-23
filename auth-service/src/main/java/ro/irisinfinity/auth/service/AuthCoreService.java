package ro.irisinfinity.auth.service;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static ro.irisinfinity.platform.common.constants.CommonMessages.BAD_CREDENTIALS;
import static ro.irisinfinity.platform.common.constants.CommonMessages.INVALID_REFRESH_TOKEN_PAYLOAD;
import static ro.irisinfinity.platform.common.constants.CommonMessages.USER_ID_MISSING;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ro.irisinfinity.auth.client.UsersClient;
import ro.irisinfinity.platform.common.dto.auth.CredentialsResponseDto;
import ro.irisinfinity.platform.common.dto.auth.EmailLookupRequestDto;
import ro.irisinfinity.platform.common.dto.auth.TokenResponse;
import ro.irisinfinity.platform.common.dto.user.UserRequestDto;
import ro.irisinfinity.platform.common.dto.user.UserResponseDto;
import ro.irisinfinity.platform.common.enums.Role;

@Service
@RequiredArgsConstructor
public class AuthCoreService {

    private static final String NOT_A_REFRESH_TOKEN = "Not a refresh token";
    private static final long ACCESS_TTL_SECONDS = 15 * 60L;

    private final UsersClient usersClient;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserResponseDto register(final UserRequestDto userRequestDto) {
        final String encodedPassword = passwordEncoder.encode(userRequestDto.password());
        final UserRequestDto sanitizedUser = userRequestDto.withPassword(encodedPassword);

        final UserResponseDto registeredUser;

        registeredUser = usersClient.createUser(sanitizedUser);

        return registeredUser;
    }

    public TokenResponse login(final String email, final String rawPassword) {
        final CredentialsResponseDto credentials;

        credentials = usersClient.findCredentials(new EmailLookupRequestDto(email));

        if (credentials == null || Boolean.FALSE.equals(credentials.enabled())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, BAD_CREDENTIALS);
        }

        final boolean passwordOk = passwordEncoder.matches(rawPassword, credentials.password());
        if (!passwordOk) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, BAD_CREDENTIALS);
        }

        final UUID userId = requireUserId(credentials.externalId());
        
        return generateTokens(userId, credentials.email(), credentials.roles());
    }

    public TokenResponse refresh(final String refreshToken) {
        final var parsed = jwtService.parseRefreshToken(refreshToken);
        final var claims = parsed.getPayload();

        final String typ = String.valueOf(claims.get("typ"));
        if (!"refresh".equals(typ)) {
            throw new ResponseStatusException(BAD_REQUEST, NOT_A_REFRESH_TOKEN);
        }

        UUID userId;
        try {
            userId = UUID.fromString(Objects.toString(claims.get("userId"), null));
        } catch (final IllegalArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, INVALID_REFRESH_TOKEN_PAYLOAD);
        }

        final String email = claims.getSubject();
        if (email == null) {
            throw new ResponseStatusException(BAD_REQUEST, INVALID_REFRESH_TOKEN_PAYLOAD);
        }

        var credentials = usersClient.findCredentials(new EmailLookupRequestDto(email));
        if (credentials == null || Boolean.FALSE.equals(credentials.enabled())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, BAD_CREDENTIALS);
        }
        final String accessToken = jwtService.createAccessToken(userId, email, credentials.roles());

        return new TokenResponse(accessToken, ACCESS_TTL_SECONDS, refreshToken);
    }

    private TokenResponse generateTokens(final UUID userId, final String email,
        final Set<Role> roles) {
        final String accessToken = jwtService.createAccessToken(userId, email, roles);
        final String refreshToken = jwtService.createRefreshToken(userId, email);

        return new TokenResponse(accessToken, ACCESS_TTL_SECONDS, refreshToken);
    }

    private UUID requireUserId(final UUID externalId) {
        if (externalId == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, USER_ID_MISSING);
        }

        return externalId;
    }
}
