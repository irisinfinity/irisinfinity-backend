package ro.irisinfinity.user.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ro.irisinfinity.common.dto.user.UserRequestDto;
import ro.irisinfinity.common.dto.user.UserResponseDto;
import ro.irisinfinity.common.enums.Sex;
import ro.irisinfinity.user.api.data.User;
import ro.irisinfinity.user.api.exception.UserAlreadyExistsException;
import ro.irisinfinity.user.api.exception.UserNotFoundException;
import ro.irisinfinity.user.api.repository.UserRepository;

class UserServiceUnitTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectMapper objectMapper;

    private User user;
    private UserRequestDto userRequestDto;
    private UserResponseDto userResponseDto;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        var userId = 1L;
        var userExternalId = UUID.randomUUID();
        var userEmail = "test@example.com";
        var userPassword = "Password123!";
        var userFirstName = "Test";
        var userLastName = "User";
        var userBirthDate = LocalDate.of(2000, 1, 1);
        var userSex = Sex.MALE;
        var userCreatedAt = LocalDateTime.now();

        user = new User();
        user.setId(userId);
        user.setExternalId(userExternalId);
        user.setEmail(userEmail);
        user.setPassword(userPassword);
        user.setFirstName(userFirstName);
        user.setLastName(userLastName);
        user.setBirthDate(userBirthDate);
        user.setSex(userSex);
        user.setCreatedAt(userCreatedAt);

        userRequestDto = new UserRequestDto(
            userEmail,
            userPassword,
            userFirstName,
            userLastName,
            userBirthDate,
            userSex
        );

        userResponseDto = new UserResponseDto(
            userExternalId,
            userEmail,
            userFirstName,
            userLastName,
            userBirthDate,
            userSex,
            userCreatedAt
        );
    }

    @Test
    @DisplayName("getUsers should return a page of mapped UserResponseDto objects")
    void getUsers_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findAll(pageable)).thenReturn(page);
        when(objectMapper.convertValue(user, UserResponseDto.class)).thenReturn(userResponseDto);

        Page<UserResponseDto> result = userService.getUsers(0, 20);

        assertEquals(1, result.getContent().size());
        assertEquals(userResponseDto, result.getContent().getFirst());
    }

    @Test
    @DisplayName("getUser should return UserResponseDto when user exists")
    void getUser_existingUser_shouldReturnDto() {
        when(userRepository.findUserByExternalId(user.getExternalId())).thenReturn(
            Optional.of(user));
        when(objectMapper.convertValue(user, UserResponseDto.class)).thenReturn(userResponseDto);

        UserResponseDto result = userService.getUser(user.getExternalId());
        assertEquals(userResponseDto, result);
    }

    @Test
    @DisplayName("getUser should throw UserNotFoundException when user does not exist")
    void getUser_nonExistingUser_shouldThrow() {
        UUID randomId = UUID.randomUUID();
        when(userRepository.findUserByExternalId(randomId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUser(randomId));
    }

    @Test
    @DisplayName("createUser should throw UserAlreadyExistsException when email already exists")
    void createUser_emailExists_shouldThrow() {
        when(userRepository.existsUserByEmail(userRequestDto.email())).thenReturn(true);
        assertThrows(UserAlreadyExistsException.class,
            () -> userService.createUser(userRequestDto));
    }

    @Test
    @DisplayName("createUser should successfully create and return UserResponseDto")
    void createUser_success_shouldReturnDto() {
        when(userRepository.existsUserByEmail(userRequestDto.email())).thenReturn(false);
        when(objectMapper.convertValue(userRequestDto, User.class)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(objectMapper.convertValue(user, UserResponseDto.class)).thenReturn(userResponseDto);

        UserResponseDto result = userService.createUser(userRequestDto);
        assertEquals(userResponseDto, result);
    }

    @Test
    @DisplayName("updateUser should throw UserNotFoundException when user does not exist")
    void updateUser_nonExisting_shouldThrow() {
        UUID randomId = UUID.randomUUID();
        when(userRepository.findUserByExternalId(randomId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
            () -> userService.updateUser(randomId, userRequestDto));
    }

    @Test
    @DisplayName("updateUser should throw UserAlreadyExistsException when email already exists")
    void updateUser_emailExists_shouldThrow() {
        var userUpdatedEmail = "updated@test.com";
        
        when(userRepository.findUserByExternalId(user.getExternalId()))
            .thenReturn(Optional.of(user));
        when(userRepository.existsUserByEmail(userUpdatedEmail)).thenReturn(true);

        var userUpdateDto = new UserRequestDto(userUpdatedEmail, user.getPassword(),
            user.getFirstName(), user.getLastName(), user.getBirthDate(), user.getSex()
        );

        assertThrows(UserAlreadyExistsException.class,
            () -> userService.updateUser(user.getExternalId(), userUpdateDto));
    }

    @Test
    @DisplayName("deleteUser should throw UserNotFoundException when user does not exist")
    void deleteUser_nonExisting_shouldThrow() {
        UUID randomId = UUID.randomUUID();
        when(userRepository.findUserByExternalId(randomId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(randomId));
    }
}