package com.bookingSystem.impl;

import com.bookingSystem.exception.InvalidPriceException;
import com.bookingSystem.exception.ResourceDoesNotExistException;
import com.bookingSystem.dto.ResourceRequest;
import com.bookingSystem.dto.ResourceResponse;
import com.bookingSystem.entity.Resource;
import com.bookingSystem.repository.ResourceRepository;
import com.bookingSystem.service.ResourceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static com.bookingSystem.helper.AuthValidator.validAdminAuth;
import static com.bookingSystem.helper.ModelMapper.*;

@RequiredArgsConstructor
@Slf4j
@Service
public class ResourceServiceImpl implements ResourceService
{
    private final ResourceRepository repository;

    @Override
    public ResourceResponse addResource(ResourceRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        validAdminAuth(authentication);
        Resource resource = mapToResource(request);
        Resource savedResource = this.repository.save(resource);
        return mapToResourceResponse(savedResource);
    }

    @Override
    public ResourceResponse updateResource(Integer id, ResourceRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        validAdminAuth(authentication);

        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) < 0)
            throw new InvalidPriceException("Invalid Price !!");

        Resource existingResource = this.repository.findById(id)
                .orElseThrow(() -> new ResourceDoesNotExistException("Resource with id: " + id + " does not exist !!"));

        existingResource.setPrice(request.getPrice());
        existingResource.setResourceName(request.getResourceName());
        Resource updatedResource = this.repository.save(existingResource);
        return mapToResourceResponse(updatedResource);
    }

    @Transactional
    @Override
    public void deleteResourceById(Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        validAdminAuth(authentication);
        Resource resource = this.repository.findById(id)
                    .orElseThrow(() -> new ResourceDoesNotExistException("Resource with id: " + id + " not exist !!"));
        this.repository.delete(resource);
    }

    @Override
    public ResourceResponse getResourceById(Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            throw new AuthenticationCredentialsNotFoundException("User is not authenticated !!");

        Resource resource = this.repository.findById(id)
                .orElseThrow(() -> new ResourceDoesNotExistException("Resource with id: " + id + " does not exist !!"));
        return mapToResourceResponse(resource);
    }

    @Override
    public List<ResourceResponse> getAllResources() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            throw new AuthenticationCredentialsNotFoundException("User is not authenticated !!");
        List<Resource> all = this.repository.findAll();
        return mapToResourceResponseList(all);
    }
}
