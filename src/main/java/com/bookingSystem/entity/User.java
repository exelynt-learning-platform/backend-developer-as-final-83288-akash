package com.bookingSystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Builder
@Validated
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(min = 3, max = 20, message = "UserName must be in 3 to 20 characters!")
    @NotNull(message = "Username is required!")
    private String userName;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Email(message = "Email is required!")
    private String email;

    @NotNull(message = "Password is required!")
    @Size(min = 8, message = "Password must be at least 8 characters!")
    private String password;
}
