package ro.irisinfinity.user.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ro.irisinfinity.common.dto.user.UserRequestDto;
import ro.irisinfinity.common.dto.user.UserResponseDto;
import ro.irisinfinity.user.api.data.User;
import ro.irisinfinity.user.api.exception.UserAlreadyExistsException;
import ro.irisinfinity.user.api.exception.UserNotFoundException;
import ro.irisinfinity.user.api.repository.UserRepository;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private UserRepository userRepository;
    private ObjectMapper objectMapper;

    public Page<UserResponseDto> getUsers(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<User> userPage = userRepository.findAll(pageable);

        return userPage.map(user -> objectMapper.convertValue(user, UserResponseDto.class));
    }

    public UserResponseDto getUser(UUID externalId) {
        Optional<User> userOptional = userRepository.findUserByExternalId(externalId);

        return userOptional
            .map(user -> objectMapper.convertValue(user, UserResponseDto.class))
            .orElseThrow(UserNotFoundException::new);
    }

    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        String userEmail = userRequestDto.email();
        boolean existsUserByEmail = userRepository.existsUserByEmail(userEmail);

        if (existsUserByEmail) {
            throw new UserAlreadyExistsException("Email already registered: " + userEmail);
        }

        User user = objectMapper.convertValue(userRequestDto, User.class);
        User savedUser = userRepository.save(user);

        log.info("User created successfully: email={}", savedUser.getEmail());
        return objectMapper.convertValue(savedUser, UserResponseDto.class);
    }

    public UserResponseDto updateUser(UUID externalId, UserRequestDto userRequestDto) {
        Optional<User> userOptional = userRepository.findUserByExternalId(externalId);

        User currentUser = userOptional.orElseThrow(UserNotFoundException::new);

        if (!currentUser.getEmail().equals(userRequestDto.email())) {
            String userUpdatedEmail = userRequestDto.email();
            boolean existsUserByEmail = userRepository.existsUserByEmail(userUpdatedEmail);

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

        User updatedUser = userRepository.save(currentUser);

        log.info("User updated successfully: id={}", updatedUser.getId());
        return objectMapper.convertValue(updatedUser, UserResponseDto.class);
    }

    public void deleteUser(UUID externalId) {
        Optional<User> userOptional = userRepository.findUserByExternalId(externalId);

        Long userId = userOptional
            .map(User::getId)
            .orElseThrow(UserNotFoundException::new);

        userRepository.deleteById(userId);
        log.info("User deleted successfully: id={}", userId);
    }
}
