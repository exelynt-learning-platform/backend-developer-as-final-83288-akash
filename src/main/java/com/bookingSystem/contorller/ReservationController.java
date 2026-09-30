package com.bookingSystem.contorller;

import com.bookingSystem.dto.ReservationRequest;
import com.bookingSystem.dto.ReservationResponse;
import com.bookingSystem.helper.PageResponse;
import com.bookingSystem.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/booking/reservations")
public class ReservationController {
    public static final Logger LOGGER = Logger.getLogger(ReservationController.class.getName());
    private final ReservationService service;

    @PostMapping
    public ResponseEntity<ReservationResponse> addReservation(@Valid @RequestBody ReservationRequest request){
        LOGGER.info("Received request to add reservation for user for resource: " + request.getResourceId());
        ReservationResponse response = this.service.addReservation(request);
        LOGGER.info("Successfully added reservation for user: " + response.getUser().getUserName() + " for resource: " + response.getResource().getResourceName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(@Valid @PathVariable("id") Integer id, @RequestBody ReservationRequest request) {
        LOGGER.info("Received received request to update reservation with id: " + id);
        ReservationResponse response = this.service.updateReservation(id, request);
        LOGGER.info("Successfully updated reservation with details: " + response);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReservationById(@PathVariable("id") Integer id){
        this.service.deleteReservation(id);
        return ResponseEntity.ok("Successfully Deleted Reservation for id: " + id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservation(@PathVariable("id") Integer id){
        LOGGER.info("Received request to get reservation with id: " + id);
        ReservationResponse response = this.service.getReservationByReservationId(id);
        LOGGER.info("Successfully sent reservation with details: " + response);
        return ResponseEntity.ok(response);
    }


    @GetMapping
    public ResponseEntity<PageResponse<ReservationResponse>> getAllReservations(
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            Pageable pageable) {
        LOGGER.info("Received request to get all reservations");
        PageResponse<ReservationResponse> allReservations = this.service.getAllReservations(pageable, minPrice, maxPrice);
        LOGGER.info("Successfully sent all reservations");
        return ResponseEntity.ok(allReservations);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<PageResponse<ReservationResponse>> getAllReservationsByUserId(
            @PathVariable("id") Integer id,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            Pageable pageable) {
        PageResponse<ReservationResponse> response = this.service.getAllReservationsByUserId(id, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/resources/{id}")
    public ResponseEntity<PageResponse<ReservationResponse>> getAllReservationsByResourceId(
            @PathVariable("id") Integer id,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            Pageable pageable) {
        PageResponse<ReservationResponse> response = this.service.getAllReservationsByResourceId(id, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{id}/status/{status}")
    public ResponseEntity<PageResponse<ReservationResponse>> getAllReservationsByUserWithStatus(
            @PathVariable("id") Integer id, @PathVariable("status") String status,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            Pageable pageable){
        LOGGER.info("Received request to find all reservations made by a user of id: " + id + " with status: " + status.toUpperCase());
        PageResponse<ReservationResponse> response = this.service.getAllReservationsByUserWithStatus(id, status, minPrice, maxPrice, pageable);
        LOGGER.info("Successfully sent all the reservations made by a user of id: " + id + " with status: " + status.toUpperCase());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/resources/{id}/status/{status}")
    public ResponseEntity<PageResponse<ReservationResponse>> getAllReservationsByResourceWithStatus(
            @PathVariable("id") Integer id,
            @PathVariable("status") String status,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            Pageable pageable){
        LOGGER.info("Received request to find all reservations made for a Resource of id: " + id + " with status: " + status.toUpperCase());
        PageResponse<ReservationResponse> response = this.service.getAllReservationsByResourceWithStatus(id, status, minPrice, maxPrice, pageable);
        LOGGER.info("Successfully sent all the reservations made for a Resource of id: " + id + " with status: " + status.toUpperCase());
        return ResponseEntity.ok(response);
    }
}
