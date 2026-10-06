package com.bookingSystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload used to authenticate an existing user")
public class LoginRequest {

    @Schema(
            description = "Email address registered with the application",
            example = "user@gmail.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Email is required !!")
    @Email(message = "Invalid email !!")
    private String email;


    @Schema(
            description = "Password of the user",
            example = "Password@123",
            format = "password",
            minLength = 8,
            maxLength = 20,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Password is required !!")
    @Size(
            min = 8,
            max = 20,
            message = "Password must be between 8 and 20 characters"
    )
    private String password;
}