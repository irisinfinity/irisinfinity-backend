package ro.irisinfinity.events.client;


import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ro.irisinfinity.platform.common.dto.users.UserResponseDto;

@FeignClient(name = "users-service")
public interface UsersClient {

    @GetMapping("/api/v1/users/{externalId}")
    UserResponseDto getUserByExternalId(@PathVariable("externalId") final UUID externalId);

}