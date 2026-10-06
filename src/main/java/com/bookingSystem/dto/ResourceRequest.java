package com.bookingSystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Validated
@Schema(description = "Request payload used to create or update a booking resource")
public class ResourceRequest {

    @Schema(
            description = "Name of the booking resource",
            example = "Conference Room A",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Resource Name is required!")
    private String resourceName;


    @Schema(
            description = "Price of the resource",
            example = "150000.00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Resource Price is required!")
    private BigDecimal price;
}