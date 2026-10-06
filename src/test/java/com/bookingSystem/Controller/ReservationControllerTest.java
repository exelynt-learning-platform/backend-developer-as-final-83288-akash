package com.bookingSystem.Controller;

import com.bookingSystem.config.SecurityConfig;
import com.bookingSystem.contorller.ReservationController;
import com.bookingSystem.dto.ReservationRequest;
import com.bookingSystem.dto.ReservationResponse;
import com.bookingSystem.dto.UserResponse;
import com.bookingSystem.entity.Resource;
import com.bookingSystem.helper.PageResponse;
import com.bookingSystem.service.ReservationService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
@Import(SecurityConfig.class)
public class ReservationControllerTest {
    @MockitoBean
    private ReservationService service;

    @MockitoBean
    private JwtAuthenticationConverter converter;

    @MockitoBean
    private JwtDecoder decoder;

    @Autowired
    private MockMvc mvc;

    @Test
    @WithMockUser(value = "admin@gmail.com", roles = "ADMIN")
    void addReservation_shouldReturn200() throws Exception{
        ReservationResponse response = ReservationResponse.builder()
                .id(1)
                .user(new UserResponse(1, "user", "user@gmail.com"))
                .resource(new Resource(1, "mobile", BigDecimal.valueOf(2000.0)))
                .status("PENDING")
                .build();

        when(service.addReservation(any(ReservationRequest.class)))
                .thenReturn(response);

        mvc.perform(post("/api/booking/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "resourceId": 1,
                                "userId": 1,
                                "startDate": "02/04/2026 10:00",
                                "endDate": "04/04/2026 11:00"
                                }
                                """))
                .andExpect(status().isCreated());
        verify(service).addReservation(any(ReservationRequest.class));
    }


    @Test
    @WithMockUser(value = "user@gmail.com", roles = "USER")
    void addReservation_shouldReturn200_forUser() throws Exception{
        ReservationResponse response = ReservationResponse.builder()
                .id(1)
                .user(new UserResponse(1, "user", "user@gmail.com"))
                .resource(new Resource(1, "mobile", BigDecimal.valueOf(2000.0)))
                .status("PENDING")
                .build();

        when(service.addReservation(any(ReservationRequest.class)))
                .thenReturn(response);

        mvc.perform(post("/api/booking/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "resourceId": 1,
                                "userId": 1,
                                "startDate": "02/04/2026 10:00",
                                "endDate": "04/04/2026 11:00"
                                }
                                """))
                .andExpect(status().isCreated());
        verify(service).addReservation(any(ReservationRequest.class));
    }

    @Test
    @WithMockUser(value = "admin@gmail.com", roles = "ADMIN")
    void updateReservation_shouldReturn200() throws Exception{
        ReservationResponse response = ReservationResponse.builder()
                .id(1)
                .user(new UserResponse(1, "user", "user@gmail.com"))
                .resource(new Resource(1, "mobile", BigDecimal.valueOf(2000.0)))
                .status("PENDING")
                .build();

        when(service.updateReservation(eq(1), any(ReservationRequest.class)))
                .thenReturn(response);

        mvc.perform(put("/api/booking/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "resourceId": 1,
                                "userId": 1,
                                "startDate": "02/04/2026 10:00",
                                "endDate": "04/04/2026 11:00"
                                }
                                """))
                .andExpect(status().isOk());
        verify(service).updateReservation(eq(1), any(ReservationRequest.class));
    }



    @Test
    @WithMockUser(value = "user@gmail.com", roles = "USER")
    void updateReservation_shouldReturn403() throws Exception{
        when(service.updateReservation(eq(1), any(ReservationRequest.class)))
                .thenThrow(
                        new AuthorizationDeniedException("Only ADMIN is allowed !!")
                );

        mvc.perform(put("/api/booking/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "resourceId": 1,
                                "userId": 1,
                                "startDate": "02/04/2026 10:00",
                                "endDate": "04/04/2026 11:00"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(value = "user@gmail.com", roles = "USER")
    void getReservationById_shouldReturn200_forUser() throws Exception{
        ReservationResponse response = ReservationResponse.builder()
                .id(1)
                .user(new UserResponse(1, "user", "user@gmail.com"))
                .resource(new Resource(1, "mobile", BigDecimal.valueOf(2000.0)))
                .status("PENDING")
                .build();

        when(service.getReservationByReservationId(eq(1)))
                .thenReturn(response);
        mvc.perform(get("/api/booking/reservations/1"))
                        .andExpect(status().isOk());
        verify(service).getReservationByReservationId(eq(1));
    }

    @Test
    @WithMockUser(value = "admin@gmail.com", roles = "ADMIN")
    void getReservationById_shouldReturn200() throws Exception{
        ReservationResponse response = ReservationResponse.builder()
                .id(1)
                .user(new UserResponse(1, "user", "user@gmail.com"))
                .resource(new Resource(1, "mobile", BigDecimal.valueOf(2000.0)))
                .status("PENDING")
                .build();

        when(service.getReservationByReservationId(eq(1)))
                .thenReturn(response);
        mvc.perform(get("/api/booking/reservations/1"))
                .andExpect(status().isOk());
        verify(service).getReservationByReservationId(eq(1));
    }


    @Test
    @WithMockUser(value = "admin@gmail.com", roles = "ADMIN")
    void getAllReservations_shouldReturn200() throws Exception{
        PageResponse<ReservationResponse> response = new PageResponse<>(
                List.of(new ReservationResponse()),
                0,
                2,
                2,
                1);

        when(service.getAllReservations(any(), any(), any()))
                .thenReturn(response);
        mvc.perform(get("/api/booking/reservations"))
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(value = "user@gmail.com", roles = "USER")
    void getAllReservations_shouldReturn403() throws Exception{
        when(service.getAllReservations(any(), any(), any()))
                .thenThrow(
                        new AuthorizationDeniedException("Only ADMIN is allowed !!")
                );
        mvc.perform(get("/api/booking/reservations"))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(value = "admin@gmail.com", roles = "ADMIN")
    void getAllReservationsByUserId_shouldReturn200() throws Exception{
        PageResponse<ReservationResponse> response = new PageResponse<>(
                List.of(new ReservationResponse()),
                0,
                2,
                2,
                1);

        when(service.getAllReservationsByUserId(any(), any(), any(), any()))
                .thenReturn(response);
        mvc.perform(get("/api/booking/reservations/users/1"))
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(value = "user@gmail.com", roles = "USER")
    void getAllReservationsByUserId_shouldReturn200_forUser() throws Exception{
        PageResponse<ReservationResponse> response = new PageResponse<>(
                List.of(new ReservationResponse()),
                0,
                2,
                2,
                1);

        when(service.getAllReservationsByUserId(any(), any(), any(), any()))
                .thenReturn(response);
        mvc.perform(get("/api/booking/reservations/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(value = "admin@gmail.com", roles = "ADMIN")
    void getAllReservationsByUserIdWithStatus_shouldReturn200() throws Exception{
        PageResponse<ReservationResponse> response = new PageResponse<>(
                List.of(new ReservationResponse()),
                0,
                2,
                2,
                1);

        when(service.getAllReservationsByUserWithStatus(any(), any(), any(), any(), any()))
                .thenReturn(response);
        mvc.perform(get("/api/booking/reservations/users/1/status/PENDING"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(value = "user@gmail.com", roles = "USER")
    void getAllReservationsByUserIdWithStatus_shouldReturn200_forUser() throws Exception{
        PageResponse<ReservationResponse> response = new PageResponse<>(
                List.of(new ReservationResponse()),
                0,
                2,
                2,
                1);

        when(service.getAllReservationsByUserWithStatus(any(), any(), any(), any(), any()))
                .thenReturn(response);
        mvc.perform(get("/api/booking/reservations/users/1/status/PENDING"))
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(value = "admin@gmail.com", roles = "ADMIN")
    void getAllReservationsByResourceId_shouldReturn200_forUser() throws Exception{
        PageResponse<ReservationResponse> response = new PageResponse<>(
                List.of(new ReservationResponse()),
                0,
                2,
                2,
                1);

        when(service.getAllReservationsByResourceId(any(), any(), any(), any()))
                .thenReturn(response);
        mvc.perform(get("/api/booking/reservations/resources/1"))
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(value = "user@gmail.com", roles = "USER")
    void getAllReservationsByResourceId_shouldReturn403() throws Exception{
        when(service.getAllReservationsByResourceId(any(), any(), any(), any()))
                .thenThrow(
                        new AuthorizationDeniedException("Only ADMIN is allowed !!")
                );
        mvc.perform(get("/api/booking/reservations/resources/1"))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(value = "admin@gmail.com", roles = "ADMIN")
    void getAllReservationsByResourceWithStatus_shouldReturn200() throws Exception{
        PageResponse<ReservationResponse> response = new PageResponse<>(
                List.of(new ReservationResponse()),
                0,
                2,
                2,
                1);

        when(service.getAllReservationsByResourceWithStatus(any(), any(), any(), any(), any()))
                .thenReturn(response);
        mvc.perform(get("/api/booking/reservations/resources/1/status/CANCELLED"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(value = "user@gmail.com", roles = "USER")
    void getAllReservationsByResourceWithStatus_shouldReturn403() throws Exception{
        when(service.getAllReservationsByResourceWithStatus(any(), any(), any(), any(), any()))
                .thenThrow(
                        new AuthorizationDeniedException("Only ADMIN is allowed !!")
                );
        mvc.perform(get("/api/booking/reservations/resources/1/status/CANCELLED"))
                .andExpect(status().isForbidden());
    }
}
