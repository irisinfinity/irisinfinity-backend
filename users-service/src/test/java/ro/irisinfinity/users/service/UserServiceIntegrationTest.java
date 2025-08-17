package ro.irisinfinity.users.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ro.irisinfinity.platform.common.dto.user.UserRequestDto;
import ro.irisinfinity.platform.common.dto.user.UserResponseDto;
import ro.irisinfinity.platform.common.enums.Sex;
import ro.irisinfinity.users.repository.UserRepository;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createAndGetUser_shouldWork() {
        UserRequestDto userCreateDto = new UserRequestDto(
            "integration@example.com", "Password123!", "Int", "Test",
            LocalDate.of(1990, 1, 1), Sex.FEMALE
        );

        UserResponseDto createdUser = userService.createUser(userCreateDto);
        var externalId = createdUser.externalId();
        assertEquals(userCreateDto.email(), createdUser.email());

        UserResponseDto fetchedUser = userService.getUser(externalId);
        assertEquals(userCreateDto.email(), fetchedUser.email());
    }

    @Test
    void updateUser_shouldWork() {
        UserRequestDto userCreateDto = new UserRequestDto(
            "update@example.com", "Password123!", "Up", "Name",
            LocalDate.of(1995, 5, 5), Sex.MALE
        );

        userService.createUser(userCreateDto);

        UserRequestDto userUpdateDto = new UserRequestDto(
            "update@example.com", "NewPass!", "Updated", "Name",
            LocalDate.of(1995, 5, 5), Sex.MALE
        );

        UserResponseDto updatedUser = userService.updateUser(
            userRepository.findAll().getFirst().getExternalId(), userUpdateDto);
        assertEquals("Updated", updatedUser.firstName());
    }

    @Test
    void deleteUser_shouldRemoveUser() {
        UserRequestDto userCreateDto = new UserRequestDto(
            "delete@example.com", "Password123!", "Del", "User",
            LocalDate.of(2000, 1, 1), Sex.FEMALE
        );

        var externalId = userService.createUser(userCreateDto).externalId();

        userService.deleteUser(externalId);

        assertTrue(userRepository.findUserByExternalId(externalId).isEmpty());
    }
}
