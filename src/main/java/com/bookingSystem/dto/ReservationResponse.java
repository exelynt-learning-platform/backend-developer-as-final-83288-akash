package com.bookingSystem.dto;

import com.bookingSystem.entity.Resource;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = {"resource", "user"})
@Builder
public class ReservationResponse
{
    private Integer id;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private UserResponse user;
    private Resource resource;
}
