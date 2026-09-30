package com.bookingSystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.validation.annotation.Validated;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Validated
public class ResourceRequest
{
    @NotNull(message = "Resource Name is required!")
    private String resourceName;

    @NotNull(message = "Resource Price is required!")
    private Double price;
}
