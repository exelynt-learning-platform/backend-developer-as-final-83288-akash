package com.bookingSystem.contorller;

import com.bookingSystem.dto.ResourceRequest;
import com.bookingSystem.dto.ResourceResponse;
import com.bookingSystem.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@Tag(
        name = "Resource Management",
        description = "APIs for creating, retrieving, updating and deleting booking resources."
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/booking/resources")
@RequiredArgsConstructor
public class ResourceController {

    public static final Logger LOGGER =
            Logger.getLogger(ResourceController.class.getName());

    private final ResourceService resourceService;


    @Operation(
            summary = "Create a new resource",
            description = "Creates a new booking resource. " +
                    "This operation is available only to administrators."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Resource created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid resource request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only administrators can create resources"
            )
    })
    @PostMapping
    public ResponseEntity<ResourceResponse> addResource(
            @Valid @RequestBody ResourceRequest request) {

        ResourceResponse response =
                this.resourceService.addResource(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @Operation(
            summary = "Update a resource",
            description = "Updates an existing booking resource. " +
                    "This operation is available only to administrators."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Resource updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid resource request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only administrators can update resources"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource not found"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ResourceResponse> updateResource(

            @Parameter(
                    description = "ID of the resource to update",
                    example = "4"
            )
            @PathVariable("id") Integer id,

            @Valid @RequestBody ResourceRequest request) {

        ResourceResponse response =
                this.resourceService.updateResource(id, request);

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Delete a resource",
            description = "Deletes an existing booking resource. " +
                    "This operation is available only to administrators."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Resource deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only administrators can delete resources"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource not found"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteResource(

            @Parameter(
                    description = "ID of the resource to delete",
                    example = "4"
            )
            @PathVariable("id") Integer id) {

        this.resourceService.deleteResourceById(id);

        return new ResponseEntity<>(HttpStatus.OK);
    }


    @Operation(
            summary = "Get resource by ID",
            description = "Retrieves a single booking resource by its ID. " +
                    "This operation is available to authenticated users."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Resource retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource not found"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResourceResponse> getResource(

            @Parameter(
                    description = "ID of the resource",
                    example = "4"
            )
            @PathVariable("id") Integer id) {

        ResourceResponse response =
                this.resourceService.getResourceById(id);

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Get all resources",
            description = "Retrieves all available booking resources. " +
                    "This operation is available to authenticated users."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Resources retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            )
    })
    @GetMapping
    public ResponseEntity<List<ResourceResponse>> getAllResources() {

        LOGGER.info("Received request to get all the resources");

        List<ResourceResponse> response =
                this.resourceService.getAllResources();

        LOGGER.info("Successfully sent all resources as a list");

        return ResponseEntity.ok(response);
    }
}