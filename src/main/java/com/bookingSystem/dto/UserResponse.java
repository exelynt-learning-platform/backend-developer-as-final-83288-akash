package com.bookingSystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Schema(description = "Response containing user account details")
public class UserResponse {

    @Schema(
            description = "Unique identifier of the user",
            example = "7"
    )
    private Integer id;

    @Schema(
            description = "Username of the user",
            example = "Akash Khabale"
    )
    private String userName;

    @Schema(
            description = "Email address of the user",
            example = "user@gmail.com"
    )
    private String email;
}