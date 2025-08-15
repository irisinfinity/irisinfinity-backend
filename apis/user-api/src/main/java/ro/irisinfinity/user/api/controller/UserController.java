package ro.irisinfinity.user.api.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.irisinfinity.common.model.user.UserRequestDto;
import ro.irisinfinity.user.api.service.UserService;

@RestController
@RequestMapping("/api/v1/")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/users")
    public String createUser(@Valid UserRequestDto userRequestDto) {
        return userRequestDto.firstName();
    }
}
