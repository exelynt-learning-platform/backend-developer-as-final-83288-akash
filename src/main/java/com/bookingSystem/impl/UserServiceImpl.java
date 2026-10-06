package com.bookingSystem.impl;

import com.bookingSystem.dto.*;
import com.bookingSystem.entity.User;
import com.bookingSystem.entity.UserRole;
import com.bookingSystem.exception.UserAlreadyExistsException;
import com.bookingSystem.exception.UserDoesNotExistException;
import com.bookingSystem.exception.UserIdConflictException;
import com.bookingSystem.repository.UserRepository;
import com.bookingSystem.service.UserService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import static com.bookingSystem.helper.AuthValidator.validAdminAuth;
import static com.bookingSystem.helper.ModelMapper.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService
{

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        User savedUser = this.userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    @Override
    public UserResponse updateUserDetailsById(Integer id, @Valid UserRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            throw new AuthenticationCredentialsNotFoundException("User is not authenticated !!");
        String username = authentication.getName();
        User authenticatedUser = this.userRepository.findByEmail(username)
                .orElseThrow(() -> new UserDoesNotExistException("User with username: " + username + " does not exist !!"));
        if (
                !(authenticatedUser.getRole().equals(UserRole.ADMIN))
                        &&
                        !(authenticatedUser.getId().equals(id))
        )
            throw new AuthorizationDeniedException("You can't modify someone else data !!");
        if (!request.getId().equals(id))
            throw new UserIdConflictException("User id in header and body must be same !!");
        User existingUser = this.userRepository.findById(id)
                .orElseThrow(() -> new UserDoesNotExistException("User with id: " + id + " does not exist !!"));
        existingUser.setUserName(request.getUserName());
        existingUser.setEmail(request.getEmail());
        existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
        User updatedUser = this.userRepository.save(existingUser);
        return mapToUserResponse(updatedUser);
    }


    @Override
    public UserResponse getUserDetailsById(Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            throw new AuthenticationCredentialsNotFoundException("User is not authenticated !!");
        String username = authentication.getName();
        User authenticatedUser = this.userRepository.findByEmail(username)
                .orElseThrow(() -> new UserDoesNotExistException("User with username: " + username + " does not exist !!"));
        if (
                !(authenticatedUser.getRole().equals(UserRole.ADMIN))
                        &&
                        !(authenticatedUser.getId().equals(id))
        )
            throw new AuthorizationDeniedException("You can not view someone else details !!");
        User existingUser = this.userRepository.findById(id)
                .orElseThrow(() -> new UserDoesNotExistException("User with id: " + id + " does not exist !!"));
        return mapToUserResponse(existingUser);
    }

    @Transactional
    @Override
    public void deleteUserById(Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        validAdminAuth(authentication);
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new UserDoesNotExistException("User with id: " + id + " does not exist !!"));
        this.userRepository.deleteById(user.getId());
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

    @Override
    public LoginResponse loginUser(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // HEADER
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        // PAYLOAD
        String username = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        boolean hasRoleAdmin = authorities.stream()
                .anyMatch((auth) -> "ROLE_ADMIN".equals(auth.getAuthority()));
        String userRole;

        if (hasRoleAdmin)
            userRole = "ADMIN";
        else
            userRole = "USER";

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(username)
                .claim("role", userRole)
                .issuedAt(now)
                .issuer("resource-booking-system")
                .audience(List.of("resource-booking-api"))
                .expiresAt(now.plusSeconds(3600))
                .build();

        // SIGNATURE
        String token = jwtEncoder
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setExpiresIn(3600);
        return response;
    }
}
