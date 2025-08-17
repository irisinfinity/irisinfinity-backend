package ro.irisinfinity.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ro.irisinfinity.platform.common.dto.user.UserRequestDto;
import ro.irisinfinity.platform.common.dto.user.UserResponseDto;

@FeignClient(name = "users-service")
public interface UsersClient {

    @PostMapping("/api/v1/users")
    UserResponseDto createUser(@RequestBody UserRequestDto createUserRequestDto);
}