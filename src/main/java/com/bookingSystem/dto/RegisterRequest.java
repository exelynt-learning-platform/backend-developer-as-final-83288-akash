package com.bookingSystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
public class RegisterRequest
{
    @NotNull(message = "Name is required!")
    private String name;
    @NotNull(message = "Email is required!")
    private String email;
    @NotNull(message = "Password is required!")
    private String password;
}
