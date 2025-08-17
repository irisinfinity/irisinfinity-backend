package ro.irisinfinity.users.service;

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
import ro.irisinfinity.platform.common.dto.user.UserRequestDto;
import ro.irisinfinity.platform.common.dto.user.UserResponseDto;
import ro.irisinfinity.platform.common.enums.Sex;
import ro.irisinfinity.users.entity.User;
import ro.irisinfinity.users.exception.UserAlreadyExistsException;
import ro.irisinfinity.users.exception.UserNotFoundException;
import ro.irisinfinity.users.repository.UsersRepository;

class UsersServiceUnitTest {

    @InjectMocks
    private UsersService usersService;

    @Mock
    private UsersRepository usersRepository;

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
        var userEnabled = true;

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
            userCreatedAt,
            userEnabled
        );
    }

    @Test
    @DisplayName("getUsers should return a page of mapped UserResponseDto objects")
    void getUsers_shouldReturnMappedPageByExternalId() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> page = new PageImpl<>(List.of(user));

        when(usersRepository.findAll(pageable)).thenReturn(page);
        when(objectMapper.convertValue(user, UserResponseDto.class)).thenReturn(userResponseDto);

        Page<UserResponseDto> result = usersService.getUsers(0, 20);

        assertEquals(1, result.getContent().size());
        assertEquals(userResponseDto, result.getContent().getFirst());
    }

    @Test
    @DisplayName("getUser should return UserResponseDto when user exists")
    void getUser_existingUser_ByExternalId_shouldReturnDto() {
        when(usersRepository.findUserByExternalId(user.getExternalId())).thenReturn(
            Optional.of(user));
        when(objectMapper.convertValue(user, UserResponseDto.class)).thenReturn(userResponseDto);

        UserResponseDto result = usersService.getUserByExternalId(user.getExternalId());
        assertEquals(userResponseDto, result);
    }

    @Test
    @DisplayName("getUser should throw UserNotFoundException when user does not exist")
    void getUser_nonExistingUser_ByExternalId_shouldThrow() {
        UUID randomId = UUID.randomUUID();
        when(usersRepository.findUserByExternalId(randomId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> usersService.getUserByExternalId(randomId));
    }

    @Test
    @DisplayName("createUser should throw UserAlreadyExistsException when email already exists")
    void createUser_emailExists_shouldThrow() {
        when(usersRepository.existsUserByEmail(userRequestDto.email())).thenReturn(true);
        assertThrows(UserAlreadyExistsException.class,
            () -> usersService.createUser(userRequestDto));
    }

    @Test
    @DisplayName("createUser should successfully create and return UserResponseDto")
    void createUser_success_shouldReturnDto() {
        when(usersRepository.existsUserByEmail(userRequestDto.email())).thenReturn(false);
        when(objectMapper.convertValue(userRequestDto, User.class)).thenReturn(user);
        when(usersRepository.save(user)).thenReturn(user);
        when(objectMapper.convertValue(user, UserResponseDto.class)).thenReturn(userResponseDto);

        UserResponseDto result = usersService.createUser(userRequestDto);
        assertEquals(userResponseDto, result);
    }

    @Test
    @DisplayName("updateUser should throw UserNotFoundException when user does not exist")
    void updateUser_nonExisting_shouldThrow() {
        UUID randomId = UUID.randomUUID();
        when(usersRepository.findUserByExternalId(randomId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
            () -> usersService.updateUser(randomId, userRequestDto));
    }

    @Test
    @DisplayName("updateUser should throw UserAlreadyExistsException when email already exists")
    void updateUser_emailExists_shouldThrow() {
        var userUpdatedEmail = "updated@test.com";

        when(usersRepository.findUserByExternalId(user.getExternalId()))
            .thenReturn(Optional.of(user));
        when(usersRepository.existsUserByEmail(userUpdatedEmail)).thenReturn(true);

        var userUpdateDto = new UserRequestDto(userUpdatedEmail, user.getPassword(),
            user.getFirstName(), user.getLastName(), user.getBirthDate(), user.getSex()
        );

        assertThrows(UserAlreadyExistsException.class,
            () -> usersService.updateUser(user.getExternalId(), userUpdateDto));
    }

    @Test
    @DisplayName("deleteUser should throw UserNotFoundException when user does not exist")
    void deleteUser_nonExisting_shouldThrow() {
        UUID randomId = UUID.randomUUID();
        when(usersRepository.findUserByExternalId(randomId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> usersService.deleteUser(randomId));
    }
}