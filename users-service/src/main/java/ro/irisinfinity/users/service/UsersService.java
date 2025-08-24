package ro.irisinfinity.users.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.irisinfinity.platform.common.dto.auth.CredentialsResponseDto;
import ro.irisinfinity.platform.common.dto.auth.EmailLookupRequestDto;
import ro.irisinfinity.platform.common.dto.users.UserRequestDto;
import ro.irisinfinity.platform.common.dto.users.UserResponseDto;
import ro.irisinfinity.users.entity.User;
import ro.irisinfinity.users.exception.UserAlreadyExistsException;
import ro.irisinfinity.users.exception.UserNotFoundException;
import ro.irisinfinity.users.repository.UsersRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepository usersRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Page<UserResponseDto> getUsers(final int pageNumber, final int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<User> userPage = usersRepository.findAll(pageable);
        return userPage.map(user -> objectMapper.convertValue(user, UserResponseDto.class));
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserByExternalId(final UUID externalId) {
        Optional<User> userOptional = usersRepository.findUserByExternalId(externalId);
        return userOptional
            .map(user -> objectMapper.convertValue(user, UserResponseDto.class))
            .orElseThrow(UserNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public CredentialsResponseDto findCredentials(
        final EmailLookupRequestDto emailLookupRequestDto) {
        String email = emailLookupRequestDto.email();
        Optional<User> userOptional = usersRepository.findUserByEmail(email);
        return userOptional
            .map(user -> objectMapper.convertValue(user, CredentialsResponseDto.class))
            .orElseThrow(UserNotFoundException::new);
    }

    @Transactional
    public UserResponseDto createUser(final UserRequestDto userRequestDto) {
        String userEmail = userRequestDto.email();
        boolean existsUserByEmail = usersRepository.existsUserByEmail(userEmail);
        if (existsUserByEmail) {
            throw new UserAlreadyExistsException("Email already registered: " + userEmail);
        }

        User user = objectMapper.convertValue(userRequestDto, User.class);
        User savedUser = usersRepository.save(user);

        log.info("User created successfully: email={}", savedUser.getEmail());
        return objectMapper.convertValue(savedUser, UserResponseDto.class);
    }

    @Transactional
    public UserResponseDto updateUser(final UUID externalId, final UserRequestDto userRequestDto) {
        Optional<User> userOptional = usersRepository.findUserByExternalId(externalId);
        User currentUser = userOptional.orElseThrow(UserNotFoundException::new);

        if (!currentUser.getEmail().equals(userRequestDto.email())) {
            String userUpdatedEmail = userRequestDto.email();
            boolean existsUserByEmail = usersRepository.existsUserByEmail(userUpdatedEmail);
            if (existsUserByEmail) {
                throw new UserAlreadyExistsException(
                    "Email already registered: " + userUpdatedEmail);
            }
        }

        currentUser.setEmail(userRequestDto.email());
        currentUser.setFirstName(userRequestDto.firstName());
        currentUser.setLastName(userRequestDto.lastName());
        currentUser.setBirthDate(userRequestDto.birthDate());
        currentUser.setSex(userRequestDto.sex());

        User updatedUser = usersRepository.save(currentUser);

        log.info("User updated successfully: id={}", updatedUser.getId());
        return objectMapper.convertValue(updatedUser, UserResponseDto.class);
    }

    @Transactional
    public void deleteUser(final UUID externalId) {
        Optional<User> userOptional = usersRepository.findUserByExternalId(externalId);
        Long userId = userOptional.map(User::getId).orElseThrow(UserNotFoundException::new);
        usersRepository.deleteById(userId);
        log.info("User deleted successfully: id={}", userId);
    }
}