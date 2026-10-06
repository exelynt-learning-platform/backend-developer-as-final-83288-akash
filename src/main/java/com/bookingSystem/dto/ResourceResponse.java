package com.bookingSystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Schema(description = "Response containing booking resource details")
public class ResourceResponse {

    @Schema(
            description = "Unique identifier of the resource",
            example = "4"
    )
    private Integer id;

    @Schema(
            description = "Name of the booking resource",
            example = "Conference Room A"
    )
    private String resourceName;

    @Schema(
            description = "Price of the resource",
            example = "150000.00"
    )
    private BigDecimal price;
}