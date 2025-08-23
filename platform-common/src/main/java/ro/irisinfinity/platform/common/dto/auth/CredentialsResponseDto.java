package ro.irisinfinity.platform.common.dto.auth;

import java.util.Set;
import java.util.UUID;
import ro.irisinfinity.platform.common.enums.Role;

public record CredentialsResponseDto(
    UUID externalId,
    String email,
    String password,
    Boolean enabled,
    Set<Role> roles
) {

}
