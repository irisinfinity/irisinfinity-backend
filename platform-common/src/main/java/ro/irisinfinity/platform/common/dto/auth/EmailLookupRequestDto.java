package ro.irisinfinity.platform.common.dto.auth;

import jakarta.validation.constraints.Email;

public record EmailLookupRequestDto(
    @Email
    String email
) {

}
