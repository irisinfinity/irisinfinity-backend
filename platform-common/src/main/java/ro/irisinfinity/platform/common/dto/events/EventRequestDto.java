package ro.irisinfinity.platform.common.dto.events;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record EventRequestDto(

    @NotBlank(message = "Code is mandatory")
    @Size(min = 3, max = 40, message = "Code must be between 3 and 40 characters")
    String code,

    @NotBlank(message = "Name is mandatory")
    @Size(min = 3, max = 150, message = "Name must be between 3 and 150 characters")
    String name,

    @NotNull(message = "Owner user id is mandatory")
    UUID ownerUserId,

    @NotNull(message = "Date is mandatory")
    @FutureOrPresent(message = "Date must be today or in the future")
    LocalDate date,

    @NotNull(message = "Location is mandatory")
    @Valid LocationDto location
) {

}