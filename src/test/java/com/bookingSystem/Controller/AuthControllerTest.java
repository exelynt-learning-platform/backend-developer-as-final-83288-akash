package com.bookingSystem.Controller;

import com.bookingSystem.config.SecurityConfig;
import com.bookingSystem.contorller.AuthController;
import com.bookingSystem.dto.LoginRequest;
import com.bookingSystem.dto.LoginResponse;
import com.bookingSystem.dto.RegisterRequest;
import com.bookingSystem.dto.UserResponse;
import com.bookingSystem.exception.UserDoesNotExistException;
import com.bookingSystem.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
public class AuthControllerTest {

    @MockitoBean
    private AuthenticationManager manager;

    @MockitoBean
    private JwtDecoder decoder;

    @MockitoBean
    private JwtEncoder encoder;

    @MockitoBean
    private UserService service;

    @MockitoBean
    private JwtAuthenticationConverter converter;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private Authentication authentication;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void addUser_shouldRegisterUserAndReturn201() throws Exception{
        UserResponse response = UserResponse.builder()
                .id(1)
                .userName("user")
                .email("user@gmail.com")
                .build();

        when(service.addUser(any(RegisterRequest.class)))
                .thenReturn(response);


        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "name": "user",
                                   "email": "user@gmail.com",
                                   "password": "user@123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("userName").value("user"))
                .andExpect(jsonPath("email").value("user@gmail.com"));
        verify(service).addUser(any(RegisterRequest.class));
    }

    @Test
    void login_shouldReturn200_forAdmin() throws Exception {
        LoginResponse response = LoginResponse.builder()
                .token("jwt-admin-token")
                .expiresIn(3600)
                .build();

        when(service.loginUser(any(LoginRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "email": "admin@gmail.com",
                        "password": "admin@123"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("token").value("jwt-admin-token"));

        verify(service).loginUser(any(LoginRequest.class));
    }

    @Test
    void login_shouldReturn200_forUser() throws Exception{
        LoginResponse response = LoginResponse.builder()
                .token("jwt-user-token")
                .expiresIn(3600)
                .build();
        when(service.loginUser(any(LoginRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "user@gmail.com",
                                    "password": "user@123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("token").value("jwt-user-token"));
        verify(service).loginUser(any(LoginRequest.class));
    }

    @Test
    void login_shouldReturn404() throws Exception{
        when(service.loginUser(any(LoginRequest.class)))
                .thenThrow(
                        new UserDoesNotExistException("User does not exist !!")
                );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "user@gmail.com",
                                    "password": "user@123"
                                }
                                """))
                .andExpect(status().isNotFound());
        verify(service).loginUser(any(LoginRequest.class));
    }
}
