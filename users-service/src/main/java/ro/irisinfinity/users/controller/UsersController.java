package ro.irisinfinity.users.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ro.irisinfinity.platform.common.dto.user.UserRequestDto;
import ro.irisinfinity.platform.common.dto.user.UserResponseDto;
import ro.irisinfinity.users.service.UsersService;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UsersController {

    private final UsersService usersService;

    @GetMapping
    public Page<UserResponseDto> getUsers(
        @RequestParam(name = "page-number", defaultValue = "0") final int pageNumber,
        @RequestParam(name = "page-size", defaultValue = "20") final int pageSize
    ) {
        return usersService.getUsers(pageNumber, pageSize);
    }

    @GetMapping("/{externalId}")
    public UserResponseDto getUserByExternalId(@PathVariable("externalId") final UUID externalId) {
        return usersService.getUserByExternalId(externalId);
    }

    @PostMapping
    public UserResponseDto createUser(@RequestBody @Valid final UserRequestDto userRequestDto) {
        return usersService.createUser(userRequestDto);
    }

    @PutMapping("/{externalId}")
    @PreAuthorize("hasRole('ADMIN') or @sec.isSelf(#externalId)")
    public UserResponseDto updateUser(
        @PathVariable("externalId") final UUID externalId,
        @RequestBody @Valid final UserRequestDto userRequestDto
    ) {
        return usersService.updateUser(externalId, userRequestDto);
    }

    @DeleteMapping("/{externalId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN') or @sec.isSelf(#externalId)")
    public void deleteUser(@PathVariable("externalId") final UUID externalId) {
        usersService.deleteUser(externalId);
    }
}
