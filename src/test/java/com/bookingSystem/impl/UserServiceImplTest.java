package com.bookingSystem.impl;

import com.bookingSystem.dto.RegisterRequest;
import com.bookingSystem.dto.UserRequest;
import com.bookingSystem.dto.UserResponse;
import com.bookingSystem.entity.User;
import com.bookingSystem.entity.UserRole;
import com.bookingSystem.exception.UserAlreadyExistsException;
import com.bookingSystem.exception.UserDoesNotExistException;
import com.bookingSystem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private Authentication authentication;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void addUser_shouldCreateUserSuccessfully(){
        RegisterRequest request = RegisterRequest.builder()
                .name("user")
                .email("user@gmail.com")
                .password("user@123")
                .build();
        User savedUser = User.builder()
                .id(1)
                .userName("user")
                .email("user@gmail.com")
                .password(encoder.encode("user@123"))
                .role(UserRole.USER)
                .build();

        when(repository.existsByEmail("user@gmail.com"))
                .thenReturn(false);
        when(repository.save(any(User.class)))
                .thenReturn(savedUser);
        UserResponse response = userService.addUser(request);
        assertNotNull(response);
        assertEquals(request.getEmail(), response.getEmail());
        assertEquals(request.getName(), response.getUserName());

        verify(repository).existsByEmail("user@gmail.com");
        verify(repository).save(any());
        SecurityContextHolder.clearContext();
    }

    @Test
    void addUser_shouldThrowException_UserAlreadyExists(){
        RegisterRequest request = RegisterRequest.builder()
                .email("user@gmail.com")
                .build();
        when(repository.existsByEmail("user@gmail.com"))
                .thenReturn(true);
        assertThrows(
                UserAlreadyExistsException.class,
                ()-> userService.addUser(request)
        );
        SecurityContextHolder.clearContext();
    }

    @Test
    void updateUerDetailsWithUserId_shouldUpdateUserDetailsSuccessfully(){
        User authenticatedUser = User.builder()
                .id(1)
                .email("user@gmail.com")
                .role(UserRole.USER)
                .build();

        UserRequest request = UserRequest.builder()
                .id(1)
                .email("user@gmail.com")
                .password("user@123")
                .build();

        User existingUser = User.builder()
                .id(1)
                .password("user@123")
                .email("user@gmail.com")
                .role(UserRole.USER)
                .build();

        User updatedUser = User.builder()
                .id(1)
                .email("user@gmail.com")
                .role(UserRole.USER)
                .build();

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        when(authentication.getName())
                .thenReturn("user@gmail.com");
        when(repository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(authenticatedUser));
        when(repository.findById(1))
                .thenReturn(Optional.of(existingUser));
        when(repository.save(existingUser))
                .thenReturn(updatedUser);

        UserResponse response = userService.updateUserDetailsById(1, request);
        assertNotNull(response);
        assertEquals(request.getEmail(), response.getEmail());

        verify(authentication).getName();
        verify(repository).findByEmail("user@gmail.com");
        verify(repository).findById(1);
        verify(repository).save(existingUser);

        SecurityContextHolder.clearContext();
    }

    @Test
    void updateUserDetailsById_shouldThrowException_whenUserDoesNotExist(){
        User authenticatedUser = User.builder()
                .id(1)
                .email("user@gmail.com")
                .role(UserRole.USER)
                .build();

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        when(authentication.getName())
                .thenReturn("user@gmail.com");
        when(repository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(authenticatedUser));
        when(repository.findById(1))
                .thenReturn(Optional.empty());

        assertThrows(
                UserDoesNotExistException.class,
                ()-> userService.updateUserDetailsById(1, null)
        );

        verify(authentication).getName();
        verify(repository).findByEmail("user@gmail.com");
        verify(repository, never()).save(any());

        SecurityContextHolder.clearContext();
    }

    @Test
    void updateUserDetailsByUserId_shouldThrowException_whenUserModifySomeoneElseDetails(){
        User authenticatedUser = User.builder()
                .id(2)
                .email("user@gmail.com")
                .role(UserRole.USER)
                .build();

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        when(authentication.getName())
                .thenReturn("user@gmail.com");
        when(repository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(authenticatedUser));


        assertThrows(
                AuthorizationDeniedException.class,
                ()-> userService.updateUserDetailsById(1, null)
        );

        verify(authentication).getName();
        verify(repository).findByEmail("user@gmail.com");
        verify(repository, never()).save(any());

        SecurityContextHolder.clearContext();
    }

    @Test
    void updateUserDetailsByUserId_shouldThrowException_whenAuthenticatedUserDetailsDoesNotExist(){
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        when(authentication.getName())
                .thenReturn("user@gmail.com");
        when(repository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserDoesNotExistException.class,
                ()-> userService.updateUserDetailsById(2, null)
        );

        verify(authentication).getName();

        SecurityContextHolder.clearContext();
    }

    @Test
    void updateUserDetailsById_shouldThrowException_whenUserIsNotAuthenticated(){
        SecurityContextHolder.clearContext();
        assertThrows(
                AuthenticationCredentialsNotFoundException.class,
                ()-> userService.updateUserDetailsById(2, null)
        );
    }

    @Test
    void deleteUserById_shouldDeleteUserByIdSuccessfully(){
        User user = new User();
        user.setId(1);
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );
        when(repository.findById(1))
                .thenReturn(Optional.of(user));
        doNothing().when(repository).deleteById(1);
        userService.deleteUserById(1);
        verify(repository).findById(1);
        SecurityContextHolder.clearContext();
    }


    @Test
    void deleteUserById_shouldThrowException_whenUserIsNotAdmin(){
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_USER")
                        )
                );

        assertThrows(
                AuthorizationDeniedException.class,
                ()-> userService.deleteUserById(1)
        );
        verify(repository, never()).deleteById(any());
        SecurityContextHolder.clearContext();
    }

    @Test
    void deleteUserById_shouldThrowException_whenUserIsNotAuthenticated(){
        SecurityContextHolder.clearContext();
        assertThrows(
                AuthenticationCredentialsNotFoundException.class,
                ()-> userService.deleteUserById(1)
        );
    }

    @Test
    void getUserDetailsByUerId_shouldReturnUserDetailsSuccessfully(){
        User authenticatedUser = User.builder()
                .id(2)
                .email("user@gmail.com")
                .role(UserRole.USER)
                .build();

        User user = User.builder()
                .id(2)
                .email("user@gmail.com")
                .role(UserRole.USER)
                .build();

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        when(authentication.getName())
                .thenReturn("user@gmail.com");
        when(repository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(authenticatedUser));
        when(repository.findById(2))
                .thenReturn(Optional.of(user));

        UserResponse response = userService.getUserDetailsById(2);
        assertNotNull(response);
        assertEquals(user.getId(), response.getId());

        verify(authentication).getName();
        verify(repository).findByEmail("user@gmail.com");
        verify(repository).findById(2);
        SecurityContextHolder.clearContext();
    }

    @Test
    void getUserDetailsByUerId_shouldThrowException_whenUserIsFetchingSomeoneElseDetails(){
        User authenticatedUser = User.builder()
                .id(2)
                .email("user@gmail.com")
                .role(UserRole.USER)
                .build();
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        when(authentication.getName())
                .thenReturn("user@gmail.com");
        when(repository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(authenticatedUser));
        assertThrows(
                AuthorizationDeniedException.class,
                ()-> userService.getUserDetailsById(5)
        );
        verify(authentication).getName();
        verify(repository).findByEmail("user@gmail.com");
        SecurityContextHolder.clearContext();
    }


    @Test
    void getUserDetailsByUerId_shouldThrowException_whenAuthenticatedUserDoesNotExist(){
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        when(authentication.getName())
                .thenReturn("user@gmail.com");
        when(repository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.empty());
        assertThrows(
                UserDoesNotExistException.class,
                ()-> userService.getUserDetailsById(5)
        );
        verify(authentication).getName();
        SecurityContextHolder.clearContext();
    }


    @Test
    void getUserDetailsByUerId_shouldThrowException_whenUserIsNotAuthenticated(){
        SecurityContextHolder.clearContext();
        assertThrows(
                AuthenticationCredentialsNotFoundException.class,
                ()-> userService.getUserDetailsById(5)
        );
    }


    @Test
    void getAllUsers_shouldReturnAllUsers(){
        User user1 = User.builder()
                .id(1)
                .userName("user")
                .email("user@gmail.com")
                .build();
        List<User> users = List.of(user1);
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );
        when(repository.findAll())
                .thenReturn(users);

        List<UserResponse> response = userService.getAllUsers();
        assertNotNull(response);
        assertEquals(users.getFirst().getId(), response.getFirst().getId());

        verify(repository).findAll();
        SecurityContextHolder.clearContext();
    }


    @Test
    void getAllUsers_shouldThrowException_whenUserIsNotAdmin(){
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_USER")
                        )
                );
        assertThrows(
                AuthorizationDeniedException.class,
                ()-> userService.getAllUsers()
        );
        SecurityContextHolder.clearContext();
    }


    @Test
    void getAllUsers_shouldThrowException_whenUserIsNotAuthenticated(){
        SecurityContextHolder.clearContext();
        assertThrows(
                AuthenticationCredentialsNotFoundException.class,
                ()-> userService.getAllUsers()
        );
    }

    @Test
    void getUserByEmail_shouldReturnUserByEmail(){
        User user = User.builder()
                .id(2).build();
        when(repository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        UserResponse response = userService.getUserByEmail("user@gmail.com");
        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
    }
}
