package com.bookingSystem.service;

import com.bookingSystem.dto.ReservationRequest;
import com.bookingSystem.dto.ReservationResponse;
import com.bookingSystem.helper.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface ReservationService {
    void deleteReservation(Integer id);
    ReservationResponse addReservation(ReservationRequest request);
    ReservationResponse updateReservation(Integer reservationId, ReservationRequest request);
    ReservationResponse getReservationByReservationId(Integer reservationId);
    PageResponse<ReservationResponse> getAllReservations(Pageable pageable, Double minPrice, Double maxPrice);
    PageResponse<ReservationResponse> getAllReservationsByUserId(Integer id, Double minPrice, Double maxPrice, Pageable pageable);
    PageResponse<ReservationResponse> getAllReservationsByResourceId(Integer id, Double minPrice, Double maxPrice, Pageable pageable);
    PageResponse<ReservationResponse> getAllReservationsByUserWithStatus(Integer id, String status, Double minPrice, Double maxPrice, Pageable pageable);
    PageResponse<ReservationResponse> getAllReservationsByResourceWithStatus(Integer id, String status, Double minPrice, Double maxPrice, Pageable pageable);
}
