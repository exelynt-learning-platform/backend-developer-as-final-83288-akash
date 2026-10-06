package com.bookingSystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.validation.annotation.Validated;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Validated
@Schema(description = "Request payload used to update user details")
public class UserRequest {

    @Schema(
            description = "ID of the user",
            example = "7",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "User id is required !!")
    private Integer id;


    @Schema(
            description = "Username of the user",
            example = "Akash Khabale",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Username is required!")
    private String userName;


    @Schema(
            description = "Email address of the user",
            example = "user@gmail.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Email is required!")
    @Email(message = "Invalid email !!")
    private String email;


    @Schema(
            description = "New password for the user account",
            example = "Password@123",
            format = "password",
            minLength = 8,
            maxLength = 20,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Password is required!")
    @Size(
            min = 8,
            max = 20,
            message = "Password must be between 8 and 20 characters !!"
    )
    private String password;
}