package ro.irisinfinity.platform.common.dto.auth;

public record LoginRequest(
    String email,
    String password) {

}