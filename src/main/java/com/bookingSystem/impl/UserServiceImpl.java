package com.bookingSystem.impl;

import com.bookingSystem.dto.RegisterRequest;
import com.bookingSystem.dto.UserRequest;
import com.bookingSystem.dto.UserResponse;
import com.bookingSystem.entity.User;
import com.bookingSystem.entity.UserRole;
import com.bookingSystem.exception.UserAlreadyExistsException;
import com.bookingSystem.exception.UserDoesNotExistException;
import com.bookingSystem.repository.UserRepository;
import com.bookingSystem.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.bookingSystem.helper.AuthValidator.validAdminAuth;
import static com.bookingSystem.helper.ModelMapper.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService
{
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    @Override
    public UserResponse addUser(RegisterRequest request) {
        String email = request.getEmail();
        boolean exists = this.userRepository.existsByEmail(email);
        if (exists)
            throw new UserAlreadyExistsException("User with email: " + email + " already exists !!");
        User user = new User();
        user.setRole(UserRole.USER);
        user.setUserName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));
        User savedUser = this.userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    @Override
    public UserResponse updateUserDetailsById(Integer id, UserRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        validAdminAuth(authentication);

        User existingUser = this.userRepository.findById(id)
                .orElseThrow(() -> new UserDoesNotExistException("User with id: " + id + " does not exist !!"));
        existingUser.setUserName(request.getUserName());
        existingUser.setEmail(request.getEmail());
        existingUser.setPassword(request.getPassword());

        User updatedUser = this.userRepository.save(existingUser);
        return mapToUserResponse(updatedUser);
    }

    @Transactional
    @Override
    public void deleteUserById(Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        validAdminAuth(authentication);

        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new UserDoesNotExistException("User with id: " + id + " does not exist !!"));
        this.userRepository.delete(user);
    }

    @Override
    public UserResponse getUserDetailsById(Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        validAdminAuth(authentication);

        User existingUser = this.userRepository.findById(id)
                .orElseThrow(() -> new UserDoesNotExistException("User with id: " + id + " does not exist !!"));
        return mapToUserResponse(existingUser);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        validAdminAuth(authentication);
        List<User> userList = this.userRepository.findAll();
        return mapToUserResponseList(userList);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = this.userRepository.findByEmail(email)
                .orElseThrow(() -> new UserDoesNotExistException("Username: " + email + " does not exist !!"));
        return mapToUserResponse(user);
    }
}
