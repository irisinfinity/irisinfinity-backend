package ro.irisinfinity.user.api.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ro.irisinfinity.common.dto.user.UserRequestDto;
import ro.irisinfinity.common.dto.user.UserResponseDto;
import ro.irisinfinity.user.api.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserResponseDto> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{externalId}")
    public UserResponseDto getUser(@PathVariable UUID externalId) {
        return userService.getUser(externalId);
    }

    @PostMapping
    public UserResponseDto createUser(@RequestBody @Valid UserRequestDto userRequestDto) {
        return userService.createUser(userRequestDto);
    }

    @PutMapping("/{externalId}")
    public UserResponseDto updateUser(
        @PathVariable UUID externalId,
        @RequestBody @Valid UserRequestDto userRequestDto
    ) {
        return userService.updateUser(externalId, userRequestDto);
    }

    @DeleteMapping("/{externalId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID externalId) {
        userService.deleteUser(externalId);
    }
}
