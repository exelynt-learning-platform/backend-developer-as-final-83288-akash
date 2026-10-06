package com.bookingSystem.service;

import com.bookingSystem.dto.ReservationRequest;
import com.bookingSystem.dto.ReservationResponse;
import com.bookingSystem.helper.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public interface ReservationService {
    void deleteReservation(Integer id);
    ReservationResponse addReservation(ReservationRequest request);
    ReservationResponse updateReservation(Integer reservationId, ReservationRequest request);
    ReservationResponse getReservationByReservationId(Integer reservationId);
    PageResponse<ReservationResponse> getAllReservations(Pageable pageable, BigDecimal minPrice, BigDecimal maxPrice);
    PageResponse<ReservationResponse> getAllReservationsByUserId(Integer id, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    PageResponse<ReservationResponse> getAllReservationsByResourceId(Integer id, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    PageResponse<ReservationResponse> getAllReservationsByUserWithStatus(Integer id, String status, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    PageResponse<ReservationResponse> getAllReservationsByResourceWithStatus(Integer id, String status, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
}
