package ro.irisinfinity.platform.common.dto.user;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import ro.irisinfinity.platform.common.enums.Sex;

public record UserResponseDto(
    UUID externalId,
    String email,
    String firstName,
    String lastName,
    LocalDate birthDate,
    Sex sex,
    LocalDateTime createdAt
) {

}