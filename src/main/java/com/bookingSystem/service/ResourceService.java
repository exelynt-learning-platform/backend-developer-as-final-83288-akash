package com.bookingSystem.service;

import com.bookingSystem.dto.ResourceRequest;
import com.bookingSystem.dto.ResourceResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ResourceService {
    ResourceResponse addResource(ResourceRequest request);
    void deleteResourceById(Integer id);
    ResourceResponse updateResource(Integer id, ResourceRequest request);
    ResourceResponse getResourceById(Integer id);
    List<ResourceResponse> getAllResources();
}
