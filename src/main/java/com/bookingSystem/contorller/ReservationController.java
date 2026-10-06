package com.bookingSystem.contorller;

import com.bookingSystem.dto.ReservationRequest;
import com.bookingSystem.dto.ReservationResponse;
import com.bookingSystem.helper.PageResponse;
import com.bookingSystem.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.logging.Logger;

@Tag(
        name = "Reservation Management",
        description = "APIs for creating, retrieving, updating, deleting, " +
                "filtering and managing resource reservations."
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/booking/reservations")
public class ReservationController {

    public static final Logger LOGGER =
            Logger.getLogger(ReservationController.class.getName());

    private final ReservationService service;


    @Operation(
            summary = "Create a new reservation",
            description = "Creates a new reservation for a resource. " +
                    "The authenticated user is associated with the reservation."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Reservation created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid reservation request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User or resource not found"
            )
    })
    @PostMapping
    public ResponseEntity<ReservationResponse> addReservation(
            @Valid @RequestBody ReservationRequest request) {

        ReservationResponse response =
                this.service.addReservation(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @Operation(
            summary = "Update a reservation",
            description = "Updates an existing reservation. " +
                    "This operation is restricted according to the configured authorization rules."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservation updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid reservation request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reservation not found"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @Parameter(
                    description = "ID of the reservation to update",
                    example = "10"
            )
            @PathVariable("id") Integer id,

            @Valid @RequestBody ReservationRequest request) {

        LOGGER.info("Received request to update reservation with id: " + id);

        ReservationResponse response =
                this.service.updateReservation(id, request);

        LOGGER.info("Successfully updated reservation with details: " + response);

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Delete a reservation",
            description = "Deletes an existing reservation by its ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservation deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reservation not found"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReservationById(
            @Parameter(
                    description = "ID of the reservation to delete",
                    example = "10"
            )
            @PathVariable("id") Integer id) {

        this.service.deleteReservation(id);

        return ResponseEntity.ok(
                "Successfully Deleted Reservation for id: " + id
        );
    }


    @Operation(
            summary = "Get reservation by ID",
            description = "Retrieves a single reservation using its reservation ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservation retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reservation not found"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservation(
            @Parameter(
                    description = "ID of the reservation",
                    example = "10"
            )
            @PathVariable("id") Integer id) {

        LOGGER.info("Received request to get reservation with id: " + id);

        ReservationResponse response =
                this.service.getReservationByReservationId(id);

        LOGGER.info("Successfully sent reservation with details: " + response);

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Get all reservations",
            description = "Retrieves all reservations with optional filtering by resource price. " +
                    "Supports pagination and sorting."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservations retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid price filter or pagination parameters"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            )
    })
    @GetMapping
    public ResponseEntity<PageResponse<ReservationResponse>> getAllReservations(

            @Parameter(
                    description = "Minimum resource price",
                    example = "10000"
            )
            @RequestParam(value = "minPrice", required = false)
            BigDecimal minPrice,

            @Parameter(
                    description = "Maximum resource price",
                    example = "100000"
            )
            @RequestParam(value = "maxPrice", required = false)
            BigDecimal maxPrice,

            @ParameterObject Pageable pageable) {

        PageResponse<ReservationResponse> allReservations =
                this.service.getAllReservations(
                        pageable,
                        minPrice,
                        maxPrice
                );

        return ResponseEntity.ok(allReservations);
    }


    @Operation(
            summary = "Get reservations by user",
            description = "Retrieves reservations belonging to a specific user. " +
                    "Supports filtering by minimum and maximum resource price, " +
                    "pagination, and sorting. " +
                    "For example, reservations can be sorted using " +
                    "'resource.price,desc'."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User reservations retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid filter, pagination or sorting parameters"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @GetMapping("/users/{id}")
    public ResponseEntity<PageResponse<ReservationResponse>> getAllReservationsByUserId(

            @Parameter(
                    description = "ID of the user",
                    example = "7"
            )
            @PathVariable("id") Integer id,

            @Parameter(
                    description = "Minimum resource price",
                    example = "150000"
            )
            @RequestParam(value = "minPrice", required = false)
            BigDecimal minPrice,

            @Parameter(
                    description = "Maximum resource price",
                    example = "300000"
            )
            @RequestParam(value = "maxPrice", required = false)
            BigDecimal maxPrice,

            @ParameterObject Pageable pageable) {

        PageResponse<ReservationResponse> response =
                this.service.getAllReservationsByUserId(
                        id,
                        minPrice,
                        maxPrice,
                        pageable
                );

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Get reservations by resource",
            description = "Retrieves reservations associated with a specific resource. " +
                    "Supports filtering by resource price, pagination, and sorting."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Resource reservations retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid filter, pagination or sorting parameters"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource not found"
            )
    })
    @GetMapping("/resources/{id}")
    public ResponseEntity<PageResponse<ReservationResponse>> getAllReservationsByResourceId(

            @Parameter(
                    description = "ID of the resource",
                    example = "4"
            )
            @PathVariable("id") Integer id,

            @Parameter(
                    description = "Minimum resource price",
                    example = "10000"
            )
            @RequestParam(value = "minPrice", required = false)
            BigDecimal minPrice,

            @Parameter(
                    description = "Maximum resource price",
                    example = "100000"
            )
            @RequestParam(value = "maxPrice", required = false)
            BigDecimal maxPrice,

            @ParameterObject Pageable pageable) {

        PageResponse<ReservationResponse> response =
                this.service.getAllReservationsByResourceId(
                        id,
                        minPrice,
                        maxPrice,
                        pageable
                );

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Get user reservations by status",
            description = "Retrieves reservations belonging to a specific user " +
                    "with the specified reservation status. " +
                    "Supports resource price filtering, pagination, and sorting."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservations retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid reservation status or request parameters"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @GetMapping("/users/{id}/status/{status}")
    public ResponseEntity<PageResponse<ReservationResponse>>
    getAllReservationsByUserWithStatus(

            @Parameter(
                    description = "ID of the user",
                    example = "7"
            )
            @PathVariable("id") Integer id,

            @Parameter(
                    description = "Reservation status. Supported values: PENDING, CONFIRMED, CANCELLED",
                    example = "CONFIRMED"
            )
            @PathVariable("status") String status,

            @Parameter(
                    description = "Minimum resource price",
                    example = "150000"
            )
            @RequestParam(value = "minPrice", required = false)
            BigDecimal minPrice,

            @Parameter(
                    description = "Maximum resource price",
                    example = "300000"
            )
            @RequestParam(value = "maxPrice", required = false)
            BigDecimal maxPrice,

            @ParameterObject Pageable pageable) {

        LOGGER.info(
                "Received request to find all reservations made by a user of id: "
                        + id + " with status: " + status.toUpperCase()
        );

        PageResponse<ReservationResponse> response =
                this.service.getAllReservationsByUserWithStatus(
                        id,
                        status,
                        minPrice,
                        maxPrice,
                        pageable
                );

        LOGGER.info(
                "Successfully sent all the reservations made by a user of id: "
                        + id + " with status: " + status.toUpperCase()
        );

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Get resource reservations by status",
            description = "Retrieves reservations associated with a specific resource " +
                    "and reservation status. Supports resource price filtering, " +
                    "pagination, and sorting."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservations retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid reservation status or request parameters"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource not found"
            )
    })
    @GetMapping("/resources/{id}/status/{status}")
    public ResponseEntity<PageResponse<ReservationResponse>>
    getAllReservationsByResourceWithStatus(

            @Parameter(
                    description = "ID of the resource",
                    example = "4"
            )
            @PathVariable("id") Integer id,

            @Parameter(
                    description = "Reservation status. Supported values: PENDING, CONFIRMED, CANCELLED",
                    example = "CONFIRMED"
            )
            @PathVariable("status") String status,

            @Parameter(
                    description = "Minimum resource price",
                    example = "10000"
            )
            @RequestParam(value = "minPrice", required = false)
            BigDecimal minPrice,

            @Parameter(
                    description = "Maximum resource price",
                    example = "100000"
            )
            @RequestParam(value = "maxPrice", required = false)
            BigDecimal maxPrice,

            @ParameterObject Pageable pageable) {

        LOGGER.info(
                "Received request to find all reservations made for a Resource of id: "
                        + id + " with status: " + status.toUpperCase()
        );

        PageResponse<ReservationResponse> response =
                this.service.getAllReservationsByResourceWithStatus(
                        id,
                        status,
                        minPrice,
                        maxPrice,
                        pageable
                );

        LOGGER.info(
                "Successfully sent all the reservations made for a Resource of id: "
                        + id + " with status: " + status.toUpperCase()
        );

        return ResponseEntity.ok(response);
    }
}