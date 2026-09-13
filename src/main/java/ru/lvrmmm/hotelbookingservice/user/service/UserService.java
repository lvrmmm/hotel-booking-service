package ru.lvrmmm.hotelbookingservice.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lvrmmm.hotelbookingservice.user.dto.request.ChangePasswordRequest;
import ru.lvrmmm.hotelbookingservice.user.dto.request.UpdateProfileRequest;
import ru.lvrmmm.hotelbookingservice.user.entity.UserRole;
import ru.lvrmmm.hotelbookingservice.user.exception.*;
import ru.lvrmmm.hotelbookingservice.user.dto.request.UserCreateRequest;
import ru.lvrmmm.hotelbookingservice.user.dto.response.UserResponse;
import ru.lvrmmm.hotelbookingservice.user.entity.User;
import ru.lvrmmm.hotelbookingservice.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createUser(UserCreateRequest request){

        if (userRepository.existsByUsername(request.username())){
            throw new UserAlreadyExistsException("Username", request.username());
        }

        if (userRepository.existsByEmail(request.email())){
            throw new UserAlreadyExistsException("Email", request.email());
        }

        String hashedPassword = passwordEncoder.encode(request.password());
        User user = request.toEntity(hashedPassword);
        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse updateUserRole(UUID id, UserRole newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (user.getRole() == UserRole.ADMIN) {
            throw new RoleChangeNotAllowedException("Cannot change the role of another administrator");
        }

        user.setRole(newRole);
        User updated = userRepository.save(user);
        return UserResponse.from(updated);
    }

    @Transactional
    public UserResponse blockUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (user.getRole() == UserRole.ADMIN) {
            throw new AdminProtectionException("Cannot block an administrator");
        }

        user.setEnabled(false);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse unblockUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setEnabled(true);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateProfile(UUID id, UpdateProfileRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.middleName() != null) user.setMiddleName(request.middleName());
        if (request.lastName() != null) user.setLastName(request.lastName());
        if (request.dateOfBirth() != null) user.setDateOfBirth(request.dateOfBirth());

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void changePassword(UUID id, ChangePasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCurrentPasswordException();
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}
