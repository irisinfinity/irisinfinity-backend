package ro.irisinfinity.platform.common.dto.auth;

import java.util.UUID;

public record CredentialsResponseDto(
    UUID externalId,
    String email,
    String password,
    Boolean enabled
) {

}
