package com.bookingSystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Validated
@Schema(description = "Request payload used to create or update a resource reservation")
public class ReservationRequest {

    @Schema(
            description = "ID of the resource to be reserved",
            example = "4",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    private Integer resourceId;


    @Schema(
            description = "ID of the user for whom the reservation is being created",
            example = "7",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    private Integer userId;


    @Schema(
            description = "Start date and time of the reservation",
            example = "02/04/2026 10:00",
            pattern = "dd/MM/yyyy HH:mm",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Start Date is required!")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime startDate;


    @Schema(
            description = "End date and time of the reservation",
            example = "02/04/2026 18:00",
            pattern = "dd/MM/yyyy HH:mm",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "End Date is required!")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime endDate;
}