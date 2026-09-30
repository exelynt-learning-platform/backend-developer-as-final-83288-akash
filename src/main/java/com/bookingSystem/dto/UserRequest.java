package com.bookingSystem.dto;

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
public class UserRequest
{
    @NotNull(message = "Username is required!")
    private String userName;

    @Email(message = "Email is required!")
    private String email;

    @NotNull(message = "Password is required!")
    @Size(min = 8, max = 20)
    private String password;

    @NotNull(message = "Role is required!")
    private String role;
}
