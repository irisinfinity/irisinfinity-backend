package ro.irisinfinity.common.dto.user;

import java.time.LocalDate;
import java.util.UUID;
import ro.irisinfinity.common.enums.Sex;

public record UserResponseDto(
    UUID externalId,
    String email,
    String firstName,
    String lastName,
    LocalDate birthDate,
    Sex sex
) {
 
}