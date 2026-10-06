package com.bookingSystem.dto;

import com.bookingSystem.entity.Resource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = {"resource", "user"})
@Builder
@Schema(description = "Response containing reservation details")
public class ReservationResponse {

    @Schema(
            description = "Unique identifier of the reservation",
            example = "15"
    )
    private Integer id;


    @Schema(
            description = "Current status of the reservation",
            example = "CONFIRMED",
            allowableValues = {
                    "PENDING",
                    "CONFIRMED",
                    "CANCELLED"
            }
    )
    private String status;


    @Schema(
            description = "Start date and time of the reservation",
            example = "02/04/2026 10:00",
            pattern = "dd/MM/yyyy HH:mm"
    )
    private LocalDateTime startDate;


    @Schema(
            description = "End date and time of the reservation",
            example = "02/04/2026 18:00",
            pattern = "dd/MM/yyyy HH:mm"
    )
    private LocalDateTime endDate;


    @Schema(
            description = "User associated with the reservation"
    )
    private UserResponse user;


    @Schema(
            description = "Resource associated with the reservation"
    )
    private Resource resource;
}