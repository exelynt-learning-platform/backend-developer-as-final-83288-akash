package com.bookingSystem.contorller;

import com.bookingSystem.dto.ResourceRequest;
import com.bookingSystem.dto.ResourceResponse;
import com.bookingSystem.service.ResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/booking/resources")
@RequiredArgsConstructor
public class ResourceController {
    public static final Logger LOGGER = Logger.getLogger(ResourceController.class.getName());
    private final ResourceService resourceService;

    @PostMapping
    public ResponseEntity<ResourceResponse> addResource(@Valid @RequestBody ResourceRequest request){
        LOGGER.info("Received request to add resource with name: " + request.getResourceName());
        ResourceResponse response = this.resourceService.addResource(request);
        LOGGER.info("Successfully added Resource: " + response.getResourceName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResourceResponse> updateResource(@PathVariable("id") Integer id, @Valid @RequestBody ResourceRequest request){
        LOGGER.info("Received request to update resource with id: " + id);
        ResourceResponse response = this.resourceService.updateResource(id, request);
        LOGGER.info("Successfully updated existing resource with id: " + response.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteResource(@PathVariable("id") Integer id){
        LOGGER.info("Received request to delete resource with id: " + id);
        this.resourceService.deleteResourceById(id);
        LOGGER.info("SUCCESS");
        return ResponseEntity.ok("Deleted Resource with id: " + id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceResponse> getResource(@PathVariable("id") Integer id){
        LOGGER.info("Received request to get resource with id: " + id);
        ResourceResponse response = this.resourceService.getResourceById(id);
        LOGGER.info("Successfully sent resource with name: " + response.getResourceName());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ResourceResponse>> getAllResources(){
        LOGGER.info("Received request to get all the resources");
        List<ResourceResponse> response = this.resourceService.getAllResources();
        LOGGER.info("Successfully sent all resources as a list");
        return ResponseEntity.ok(response);
    }
}
