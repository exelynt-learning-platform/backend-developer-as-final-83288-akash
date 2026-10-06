package com.bookingSystem.contorller;

import com.bookingSystem.dto.LoginRequest;
import com.bookingSystem.dto.LoginResponse;
import com.bookingSystem.dto.RegisterRequest;
import com.bookingSystem.dto.UserResponse;
import com.bookingSystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Authentication",
        description = "APIs for user authentication and registration."
)
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final UserService userService;


    @Operation(
            summary = "User login",
            description = "Authenticates a user using their email and password " +
                    "and returns a JWT access token."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful. JWT access token returned."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid login request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid email or password"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request) {

        LoginResponse response =
                this.userService.loginUser(request);

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account. " +
                    "The user role is assigned by the application and " +
                    "cannot be supplied through the registration request."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid registration request"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User with the provided email already exists"
            )
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> addUser(
            @Valid @RequestBody RegisterRequest request) {

        UserResponse response =
                this.userService.addUser(request);

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED
        );
    }
}