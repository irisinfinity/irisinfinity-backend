package ro.irisinfinity.platform.common.dto.auth;

public record TokenResponse(
    String accessToken,
    long expiresInSeconds,
    String refreshToken) {

}