package ro.irisinfinity.auth.service;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static ro.irisinfinity.platform.common.constants.CommonMessages.BAD_CREDENTIALS;
import static ro.irisinfinity.platform.common.constants.CommonMessages.INVALID_REFRESH_TOKEN_PAYLOAD;
import static ro.irisinfinity.platform.common.constants.CommonMessages.USER_ID_MISSING;

import feign.FeignException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
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

@Service
@RequiredArgsConstructor
public class AuthCoreService {

    private static final String NOT_A_REFRESH_TOKEN = "Not a refresh token";
    private static final long ACCESS_TTL_SECONDS = 15 * 60L;

    private final UsersClient usersClient;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;

    public TokenResponse login(String email, String rawPassword) {
        final CredentialsResponseDto credentials;
        try {
            credentials = usersClient.findCredentials(new EmailLookupRequestDto(email));
        } catch (FeignException e) {
            throw propagateFeign(e);
        }

        if (credentials == null || Boolean.FALSE.equals(credentials.enabled())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, BAD_CREDENTIALS);
        }

        final boolean passwordOk = passwordEncoder.matches(rawPassword, credentials.password());
        if (!passwordOk) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, BAD_CREDENTIALS);
        }

        final String userId = requireUserId(credentials.externalId());
        return generateTokens(userId, credentials.email());
    }

    public TokenResponse refresh(String refreshToken) {
        final var parsed = jwtService.parseRefreshToken(refreshToken);
        final var claims = parsed.getPayload();

        final String typ = String.valueOf(claims.get("typ"));
        if (!"refresh".equals(typ)) {
            throw new ResponseStatusException(BAD_REQUEST, NOT_A_REFRESH_TOKEN);
        }

        final String userId = Objects.toString(claims.get("userId"), null);
        final String email = claims.getSubject();
        if (userId == null || email == null) {
            throw new ResponseStatusException(BAD_REQUEST, INVALID_REFRESH_TOKEN_PAYLOAD);
        }

        final String accessToken = jwtService.createAccessToken(userId, email);
        return new TokenResponse(accessToken, ACCESS_TTL_SECONDS, refreshToken);
    }

    public TokenResponse register(UserRequestDto userRequestDto) {
        final String encodedPassword = passwordEncoder.encode(userRequestDto.password());
        final UserRequestDto sanitizedUser = userRequestDto.withPassword(encodedPassword);

        final UserResponseDto registeredUser;
        try {
            registeredUser = usersClient.createUser(sanitizedUser);
        } catch (FeignException e) {
            throw propagateFeign(e);
        }

        final String userId = requireUserId(registeredUser.externalId());
        
        return generateTokens(userId, registeredUser.email());
    }

    private TokenResponse generateTokens(String userId, String email) {
        final String accessToken = jwtService.createAccessToken(userId, email);
        final String refreshToken = jwtService.createRefreshToken(userId, email);
        return new TokenResponse(accessToken, ACCESS_TTL_SECONDS, refreshToken);
    }

    private String requireUserId(Object externalId) {
        if (externalId == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, USER_ID_MISSING);
        }
        return externalId.toString();
    }

    private ResponseStatusException propagateFeign(FeignException ex) {
        final HttpStatus status = HttpStatus.resolve(ex.status());
        final String message = extractFeignMessage(ex);
        return new ResponseStatusException(
            status != null ? status : HttpStatus.BAD_GATEWAY,
            message,
            ex
        );
    }

    private String extractFeignMessage(FeignException ex) {
        try {
            if (ex.responseBody().isPresent()) {
                var bytes = ex.responseBody().get();
                var copy = new byte[bytes.remaining()];
                bytes.get(copy);
                String body = new String(copy, StandardCharsets.UTF_8).trim();
                if (!body.isEmpty()) {
                    return body;
                }
            }
        } catch (Throwable ignored) {
        }
        try {
            String utf8 = ex.contentUTF8();
            if (utf8 != null && !utf8.isBlank()) {
                return utf8.trim();
            }
        } catch (Throwable ignored) {
        }
        return ex.getMessage();
    }
}
