package ro.irisinfinity.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ro.irisinfinity.platform.common.dto.auth.CredentialsResponseDto;
import ro.irisinfinity.platform.common.dto.auth.EmailLookupRequestDto;
import ro.irisinfinity.platform.common.dto.users.UserRequestDto;
import ro.irisinfinity.platform.common.dto.users.UserResponseDto;

@FeignClient(name = "users-service")
public interface UsersClient {

    @PostMapping("/api/v1/users")
    UserResponseDto createUser(@RequestBody final UserRequestDto userRequestDto);

    @PostMapping("/api/v1/internal/users/credentials")
    CredentialsResponseDto findCredentials(
        @RequestBody final EmailLookupRequestDto emailLookupRequestDto);
}