package ro.irisinfinity.users.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.irisinfinity.platform.common.dto.auth.CredentialsResponseDto;
import ro.irisinfinity.platform.common.dto.auth.EmailLookupRequestDto;
import ro.irisinfinity.users.service.UsersService;

@RestController
@RequestMapping("/api/v1/internal/users")
@RequiredArgsConstructor
public class UsersInternalController {

    private final UsersService usersService;

    @PostMapping("/credentials")
    public CredentialsResponseDto findCredentials(
        @RequestBody final EmailLookupRequestDto emailLookupRequestDto) {
        return usersService.findCredentials(emailLookupRequestDto);
    }
}
