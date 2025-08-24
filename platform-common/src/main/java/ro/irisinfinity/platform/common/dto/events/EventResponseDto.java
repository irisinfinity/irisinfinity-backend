package ro.irisinfinity.platform.common.dto.events;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import ro.irisinfinity.platform.common.dto.users.UserResponseDto;

public record EventResponseDto(
    Long id,
    String code,
    String name,
    LocalDate date,
    UUID ownerUserId,
    LocationDto location,
    List<UserResponseDto> participants
) {

}