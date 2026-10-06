package com.bookingSystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@RequiredArgsConstructor
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@Schema(description = "Request payload used to register a new user")
public class RegisterRequest {

    @Schema(
            description = "Full name of the user",
            example = "Akash Khabale",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Name is required!")
    private String name;


    @Schema(
            description = "Email address for the new user account",
            example = "user@gmail.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Email is required!")
    private String email;


    @Schema(
            description = "Password for the new user account",
            example = "Password@123",
            format = "password",
            minLength = 8,
            maxLength = 20,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Password is required!")
    private String password;
}