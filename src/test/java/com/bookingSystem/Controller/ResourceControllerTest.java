package com.bookingSystem.Controller;

import com.bookingSystem.config.SecurityConfig;
import com.bookingSystem.contorller.ResourceController;
import com.bookingSystem.dto.ResourceRequest;
import com.bookingSystem.dto.ResourceResponse;
import com.bookingSystem.service.ResourceService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResourceController.class)
@Import(SecurityConfig.class)
public class ResourceControllerTest {

    @MockitoBean
    private ResourceService service;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private JwtAuthenticationConverter converter;

    @MockitoBean
    private JwtDecoder decoder;

    @Test
    @WithMockUser(value = "admin@gmail.com", roles = "ADMIN")
     void addResource_shouldReturn200() throws Exception{
        ResourceResponse response = ResourceResponse.builder()
                .id(1)
                .resourceName("iPhone-15pro")
                .price(BigDecimal.valueOf(2000.0))
                .build();

        when(service.addResource(any(ResourceRequest.class)))
                .thenReturn(response);

        mvc.perform(post("/api/booking/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "resourceName": "iPhone-15pro",
                            "price": "2000.0"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("price").value(2000.0));
        verify(service).addResource(any(ResourceRequest.class));
    }

    @Test
    @WithMockUser(value = "user@gmail.com", roles = "USER")
    void addResource_shouldReturn403() throws Exception{
        when(service.addResource(any(ResourceRequest.class)))
                .thenThrow(
                        new AuthorizationDeniedException("Only ADMIN is allowed !!")
                );

        mvc.perform(post("/api/booking/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            "resourceName": "Samsung s22",
                            "price": "4000.0"
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void updateResource_shouldReturn200() throws Exception {
        ResourceResponse response = ResourceResponse.builder()
                .id(1)
                .resourceName("iPhone-15pro")
                .price(BigDecimal.valueOf(2000.0))
                .build();

        when(service.updateResource(eq(1), any(ResourceRequest.class)))
                .thenReturn(response);

        mvc.perform(put("/api/booking/resources/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "resourceName": "iPhone-15pro",
                        "price": 2000.0
                    }
                    """))
                .andExpect(status().isOk());

        verify(service).updateResource(eq(1), any(ResourceRequest.class));
    }

    @Test
    @WithMockUser(value = "user@gmail.com", roles = "USER")
    void updateResource_shouldReturn403() throws Exception{
        when(service.updateResource(eq(1), any(ResourceRequest.class)))
                .thenThrow(new AuthorizationDeniedException("Only ADMIN is allowed !!"));
        mvc.perform(put("/api/booking/resources/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "resourceName": "kia",
                            "price": 5000.0
                        }
                        """)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(value = "admin@gmail.com", roles = "ADMIN")
    void deleteResource_shouldReturn200() throws Exception{
        doNothing().when(service).deleteResourceById(1);
        mvc.perform(delete("/api/booking/resources/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(value = "user@gmail.com", roles = "USER")
    void deleteResource_shouldReturn403() throws Exception{
        mvc.perform(delete("/api/booking/resources/1"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(value = "admin@gmail.com", roles = "ADMIN")
    void getResourceByResourceId_shouldReturn200() throws Exception{
        ResourceResponse response = ResourceResponse.builder()
                .id(1)
                .price(BigDecimal.valueOf(2000.0))
                .resourceName("Mobile")
                .build();
        when(service.getResourceById(1))
                .thenReturn(response);
        mvc.perform(get("/api/booking/resources/1"))
                        .andExpect(status().isOk());
        verify(service).getResourceById(1);
    }


    @Test
    @WithMockUser(value = "user@gmail.com", roles = "USER")
    void getResourceByResourceId_shouldReturn200_forUser() throws Exception{
        ResourceResponse response = ResourceResponse.builder()
                .id(1)
                .price(BigDecimal.valueOf(2000.0))
                .resourceName("Computer")
                .build();
        when(service.getResourceById(1))
                .thenReturn(response);
        mvc.perform(get("/api/booking/resources/1"))
                .andExpect(status().isOk());
        verify(service).getResourceById(1);
    }

    @Test
    @WithMockUser(value = "admin@gmail.com", roles = "ADMIN")
    void getAllResources_shouldReturn200() throws Exception{
        List<ResourceResponse> response = List.of(new ResourceResponse(1,"printer", BigDecimal.valueOf(3000.0)));
        when(service.getAllResources())
                .thenReturn(response);
        mvc.perform(get("/api/booking/resources"))
                        .andExpect(status().isOk());
        verify(service).getAllResources();
    }

    @Test
    @WithMockUser(value = "user@gmail.com", roles = "USER")
    void getAllResources_shouldReturn200_forUser() throws Exception{
        List<ResourceResponse> response = List.of(new ResourceResponse(1,"Scooter", BigDecimal.valueOf(30000.0)));
        when(service.getAllResources())
                .thenReturn(response);
        mvc.perform(get("/api/booking/resources"))
                .andExpect(status().isOk());
        verify(service).getAllResources();
    }
}
