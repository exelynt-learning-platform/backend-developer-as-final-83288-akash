package com.bookingSystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response returned after successful user authentication")
public class LoginResponse {

    @Schema(
            description = "JWT access token used to authenticate subsequent API requests",
            example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGdtYWlsLmNvbSJ9.signature",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String token;

    @Schema(
            description = "Number of seconds until the JWT access token expires",
            example = "3600",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer expiresIn;
}