package com.bookingSystem.Controller;

import com.bookingSystem.config.SecurityConfig;
import com.bookingSystem.contorller.UserController;
import com.bookingSystem.dto.UserRequest;
import com.bookingSystem.dto.UserResponse;
import com.bookingSystem.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService service;

    @MockitoBean
    private JwtAuthenticationConverter converter;

    @MockitoBean
    private JwtDecoder decoder;

    @Test
    @WithMockUser(username = "user@gmail.com", roles = "USER")
    void getUserById_forUser_shouldReturn200() throws Exception{
        UserResponse response = UserResponse.builder()
                .id(1)
                .userName("Akash")
                .email("user@gmail.com")
                .build();

        when(service.getUserDetailsById(1))
                .thenReturn(response);

        mockMvc.perform(get("/api/booking/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@gmail.com"));

        verify(service).getUserDetailsById(1);
    }

    @Test
    @WithMockUser(username = "user@gmail.com", roles = "USER")
    void getUserById_forUser_shouldReturnForbidden_whenUserIsAccessingSomeoneElseDetails() throws Exception{
        when(service.getUserDetailsById(1))
                .thenThrow(
                        new AuthorizationDeniedException("You can not view someone else details !!"));

        mockMvc.perform(get("/api/booking/users/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Authorization Failure"));
        verify(service).getUserDetailsById(1);
    }

    @Test
    @WithMockUser(username = "akash@gmail.com", roles = "ADMIN")
    void getUserById_forAdmin_shouldReturn200() throws Exception{
        UserResponse response = UserResponse.builder()
                .id(2)
                .userName("Sam")
                .email("sam@gmail.com")
                .build();

        when(service.getUserDetailsById(2))
                .thenReturn(response);

        mockMvc.perform(get("/api/booking/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.email").value("sam@gmail.com"));

        verify(service).getUserDetailsById(2);
    }

    @Test
    @WithMockUser(username = "user@gmail.com", roles = "ANYONE")
    void getUserById_forAnyone_shouldThrowException() throws Exception{
        mockMvc.perform(get("/api/booking/users/1"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }


    @Test
    @WithMockUser(username = "admin@gmai.com", roles = "ADMIN")
    void getAllUsers_shouldReturnAllUsers_forAdmin() throws Exception{
        List<UserResponse> response = List.of(new UserResponse());
        when(service.getAllUsers())
                .thenReturn(response);
        mockMvc.perform(get("/api/booking/users"))
                .andExpect(status().isOk());
        verify(service).getAllUsers();
    }

    @Test
    @WithMockUser(username = "user@gmail.com", roles = "USER")
    void getAllUsers_shouldReturnForbidden_forUser() throws Exception{
        when(service.getAllUsers())
                .thenThrow(
                        new AuthorizationDeniedException("Only ADMIN is allowed !!")
                );
        mockMvc.perform(get("/api/booking/users"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(username = "user@gmail.com", roles = "ANYONE")
    void getAllUsers_shouldReturnForbidden_forAnyoneOtherThanUserAndAdmin() throws Exception{
        when(service.getAllUsers())
                .thenThrow(
                        new AuthorizationDeniedException("Only ADMIN is allowed !!")
                );
        mockMvc.perform(get("/api/booking/users"))
                        .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(username = "user@gmail.com", roles = "ADMIN")
    void deleteUserById_shouldReturn200() throws Exception{
        doNothing().when(service).deleteUserById(1);
        mockMvc.perform(delete("/api/booking/users/1"))
                .andExpect(status().isOk());
        verify(service).deleteUserById(1);
    }

    @Test
    @WithMockUser(username = "user@gmail.com", roles = "USER")
    void deleteUserById_shouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/booking/users/1"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(username = "user@gmail.com", roles = "ADMIN")
    void updateUserById_shouldReturn200_forAdmin() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(1)
                .userName("user")
                .email("userNew@gmail.com")
                .build();

        when(service.updateUserDetailsById(eq(1), any(UserRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        put("/api/booking/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "id": 1,
                                "userName": "user",
                                "email": "userNew@gmail.com",
                                "password": "user@123"
                            }
                            """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("userNew@gmail.com"));

        verify(service).updateUserDetailsById(eq(1), any(UserRequest.class));
    }

    @Test
    @WithMockUser(username = "user@gmail.com", roles = "USER")
    void updateUserById_shouldReturn200_forUser() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(1)
                .userName("user")
                .email("userNew@gmail.com")
                .build();

        when(service.updateUserDetailsById(eq(1), any(UserRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        put("/api/booking/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "id": 1,
                                "userName": "user",
                                "email": "userNew@gmail.com",
                                "password": "user@123"
                            }
                            """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("userNew@gmail.com"));

        verify(service).updateUserDetailsById(eq(1), any(UserRequest.class));
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void updateUser_shouldReturn400_whenUnknownPropertyIsProvided() throws Exception {
        mockMvc.perform(put("/api/booking/users/13")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "userName": "Sneha",
                                "id": 13,
                                "email": "sneha@gmail.com",
                                "password": "Sneha@123",
                                "role": "ADMIN"
                            }
                            """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }


    @Test
    @WithMockUser(username = "someone@gmail.com", roles = "USER")
    void updateUserDetails_shouldReturnForbidden_forUserUpdatingSomeoneElseDetails() throws Exception {
        when(service.updateUserDetailsById(eq(1), any(UserRequest.class)))
                .thenThrow(
                        new AuthorizationDeniedException("You can't update someone else details !!")
                );

        mockMvc.perform(
                        put("/api/booking/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "id": 1,
                                "userName": "user",
                                "email": "userNew@gmail.com",
                                "password": "user@123"
                            }
                            """)
                )
                .andExpect(status().isForbidden());
    }
}
