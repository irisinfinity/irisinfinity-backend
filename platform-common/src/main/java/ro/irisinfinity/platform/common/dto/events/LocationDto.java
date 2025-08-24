package ro.irisinfinity.platform.common.dto.events;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LocationDto(

    @NotBlank(message = "Location name is mandatory")
    @Size(min = 2, max = 150, message = "Location name must be between 2 and 150 characters")
    String name,

    @Size(max = 255, message = "Address must not exceed 255 characters")
    String address
) {

}