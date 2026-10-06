package com.bookingSystem.impl;

import com.bookingSystem.dto.ResourceRequest;
import com.bookingSystem.dto.ResourceResponse;
import com.bookingSystem.entity.Resource;
import com.bookingSystem.exception.InvalidPriceException;
import com.bookingSystem.exception.ResourceDoesNotExistException;
import com.bookingSystem.repository.ResourceRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ResourceServiceImplTest {

    @Mock
    private Authentication authentication;

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private ResourceServiceImpl resourceService;

    @Test
    void addResource_shouldCreateResource(){
        ResourceRequest request = ResourceRequest.builder()
                .resourceName("Samsung s23 ultra")
                .price(BigDecimal.valueOf(3000.0))
                .build();

        Resource savedResource = Resource.builder()
                .id(1)
                .resourceName("Samsung s23 ultra")
                .price(BigDecimal.valueOf(3000.0))
                .build();
        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(resourceRepository.save(any(Resource.class)))
                .thenReturn(savedResource);
        ResourceResponse response = resourceService.addResource(request);
        assertNotNull(response);
        assertEquals(savedResource.getPrice(), response.getPrice());

        verify(resourceRepository).save(any(Resource.class));
        SecurityContextHolder.clearContext();
    }

    @Test
    void addResource_shouldThrowException_whenUserIsNotAdmin(){
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
                ()-> resourceService.addResource(null)
        );

        SecurityContextHolder.clearContext();
    }

    @Test
    void addResource_shouldThrowException_whenUserIsNotAuthenticated(){
        SecurityContextHolder.clearContext();
        assertThrows(
                AuthenticationCredentialsNotFoundException.class,
                ()-> resourceService.addResource(null)
        );
    }

    @Test
    void updateResource_shouldUpdateExistingResourceSuccessfully(){
        ResourceRequest request = ResourceRequest.builder()
                .price(BigDecimal.valueOf(2000.0))
                .resourceName("HP Laptop 15sDu3xxx")
                .build();

        Resource existingResource = Resource.builder()
                .id(1)
                .price(BigDecimal.valueOf(1000.0))
                .resourceName("HP Laptop 14sDu3xxx")
                .build();

        Resource updatedResource = Resource.builder()
                .id(1)
                .price(BigDecimal.valueOf(2000.0))
                .resourceName("HP Laptop 15sDu3xxx")
                .build();

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );

        when(resourceRepository.findById(1))
                .thenReturn(Optional.of(existingResource));
        when(resourceRepository.save(existingResource))
                .thenReturn(updatedResource);

        ResourceResponse response = resourceService.updateResource(1, request);
        assertNotNull(response);
        assertEquals(request.getPrice(), response.getPrice());
        assertEquals(request.getResourceName(), response.getResourceName());

        verify(resourceRepository).findById(1);
        verify(resourceRepository).save(existingResource);

        SecurityContextHolder.clearContext();
    }

    @Test
    void updateResource_shouldThrowException_whenPriceIsInvalid(){
        ResourceRequest request = ResourceRequest.builder()
                .price(null)
                .resourceName("HP Laptop 15sDu3xxx")
                .build();

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );

        assertThrows(
                InvalidPriceException.class,
                ()-> resourceService.updateResource(1, request)
        );

        verify(resourceRepository, never()).findById(1);
        verify(resourceRepository, never()).save(any());

        SecurityContextHolder.clearContext();
    }

    @Test
    void updateResource_shouldThrowException_whenResourceDoesNotExist(){
        ResourceRequest request = ResourceRequest.builder()
                .price(BigDecimal.valueOf(2000.0))
                .resourceName("HP Laptop 15sDu3xxx")
                .build();

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );

        when(resourceRepository.findById(1))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceDoesNotExistException.class,
                ()-> resourceService.updateResource(1, request)
        );

        verify(resourceRepository).findById(1);
        verify(resourceRepository, never()).save(any());

        SecurityContextHolder.clearContext();
    }

    @Test
    void updateResource_shouldThrowException_whenUserIsNotAdmin(){
        ResourceRequest request = ResourceRequest.builder()
                .price(BigDecimal.valueOf(2000.0))
                .resourceName("HP Laptop 15sDu3xxx")
                .build();

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
                ()-> resourceService.updateResource(1, request)
        );

        verify(resourceRepository, never()).findById(1);
        verify(resourceRepository, never()).save(any());

        SecurityContextHolder.clearContext();
    }

    @Test
    void updateResource_shouldThrowException_whenUserIsNotAuthenticated(){
        SecurityContextHolder.clearContext();
        assertThrows(
                AuthenticationCredentialsNotFoundException.class,
                ()-> resourceService.updateResource(1, new ResourceRequest())
        );

        verify(resourceRepository, never()).findById(1);
        verify(resourceRepository, never()).save(any());
    }

    @Test
    void deleteResource_shouldSuccessfullyDeleteResourceById(){
        Resource resource = Resource.builder()
                .id(1)
                .price(BigDecimal.valueOf(2000.0))
                .build();

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );
        when(resourceRepository.findById(1))
                .thenReturn(Optional.of(resource));
        doNothing().when(resourceRepository).delete(resource);
        resourceService.deleteResourceById(1);
        verify(resourceRepository).findById(1);
        SecurityContextHolder.clearContext();
    }


    @Test
    void deleteResource_shouldThrowException_whenUserIsNotAdmin(){
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
                ()-> resourceService.deleteResourceById(1)
        );

        verify(resourceRepository, never()).findById(1);
        SecurityContextHolder.clearContext();
    }


    @Test
    void deleteResource_shouldThrowException_whenUserIsNotAuthenticated(){
        SecurityContextHolder.clearContext();
        assertThrows(
                AuthenticationCredentialsNotFoundException.class,
                ()-> resourceService.deleteResourceById(1)
        );

        verify(resourceRepository, never()).findById(1);
    }

    @Test
    void getResourceById_shouldReturnResourceById(){
        Resource existingResource = Resource.builder()
                .id(1)
                .build();

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(resourceRepository.findById(1))
                .thenReturn(Optional.of(existingResource));

        ResourceResponse response = resourceService.getResourceById(1);
        assertNotNull(response);

        verify(resourceRepository).findById(1);
        SecurityContextHolder.clearContext();
    }

    @Test
    void getResourceById_shouldThrowException_whenResourceDoesNotExist(){
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(resourceRepository.findById(1))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceDoesNotExistException.class,
                ()-> resourceService.getResourceById(1)
        );

        verify(resourceRepository).findById(1);
        SecurityContextHolder.clearContext();
    }


    @Test
    void getResourceById_shouldThrowException_whenUserIsNotAuthenticated(){
        SecurityContextHolder.clearContext();
        assertThrows(
                AuthenticationCredentialsNotFoundException.class,
                ()-> resourceService.getResourceById(1)
        );

        verify(resourceRepository, never()).findById(1);
    }

    @Test
    void getAllResources_shouldReturnAllResources(){
        List<Resource> resources = List.of(new Resource(), Resource.builder().build());
        SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
        when(resourceRepository.findAll())
                .thenReturn(resources);
        List<ResourceResponse> response = resourceService.getAllResources();
        assertNotNull(response);
        verify(resourceRepository).findAll();
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllResources_shouldThrowException_whenUserIsNotAuthenticated(){
        SecurityContextHolder.clearContext();
        assertThrows(
                AuthenticationCredentialsNotFoundException.class,
                ()-> resourceService.getAllResources()
        );

    }
}
