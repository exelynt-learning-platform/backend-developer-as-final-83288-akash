package com.bookingSystem.helper;

import com.bookingSystem.dto.*;
import com.bookingSystem.entity.*;
import org.springframework.data.domain.Page;

import java.util.List;

public class ModelMapper
{
    public static UserResponse mapToUserResponse(User user){
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUserName(user.getUserName());
        response.setEmail(user.getEmail());
        return response;
    }

    public static Resource mapToResource(ResourceRequest request){
        Resource resource = new Resource();
        resource.setResourceName(request.getResourceName());
        resource.setPrice(request.getPrice());
        return resource;
    }

    public static ResourceResponse mapToResourceResponse(Resource resource){
        ResourceResponse response = new ResourceResponse();
        response.setId(resource.getId());
        response.setPrice(resource.getPrice());
        response.setResourceName(resource.getResourceName());
        return response;
    }

    public static Reservation mapToReservation(ReservationRequest request) {
        Reservation reservation = new Reservation();
        reservation.setStartDate(request.getStartDate());
        reservation.setEndDate(request.getEndDate());
        return reservation;
    }

    public static ReservationResponse mapToReservationResponse(Reservation reservation){
        ReservationResponse response = new ReservationResponse();
        response.setId(reservation.getId());
        response.setStatus(reservation.getStatus().toString());
        response.setStartDate(reservation.getStartDate());
        response.setEndDate(reservation.getEndDate());
        response.setResource(reservation.getResource());
        response.setUser(mapToUserResponse(reservation.getUser()));
        return response;
    }

    public static List<ResourceResponse> mapToResourceResponseList(List<Resource> resourceList){
        return resourceList.stream()
                .map(ModelMapper::mapToResourceResponse)
                .toList();
    }

    public static List<ReservationResponse> mapToReservationResponseList(List<Reservation> reservationList){
        return reservationList.stream()
                .map(ModelMapper::mapToReservationResponse)
                .toList();
    }

    public static List<UserResponse> mapToUserResponseList(List<User> userList){
        return userList.stream()
                .map(ModelMapper::mapToUserResponse)
                .toList();
    }

    public static PageResponse<ReservationResponse> mapToReservationResponsePageResponse(Page<ReservationResponse> reservations){
        return  new PageResponse<>(
                reservations.getContent(),
                reservations.getNumber(),
                reservations.getSize(),
                reservations.getTotalElements(),
                reservations.getTotalPages());
    }
}
