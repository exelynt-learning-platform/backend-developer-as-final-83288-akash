package com.bookingSystem.contorller;

import com.bookingSystem.dto.UserRequest;
import com.bookingSystem.dto.UserResponse;
import com.bookingSystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "User Management",
        description = "APIs for retrieving, updating and deleting user accounts."
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/booking/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;


    @Operation(
            summary = "Update user details",
            description = "Updates the details of an existing user. " +
                    "A USER can update only their own details, while an ADMIN " +
                    "can update any user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User details updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid user request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUserDetails(

            @Parameter(
                    description = "ID of the user to update",
                    example = "7"
            )
            @PathVariable("id") Integer id,

            @Valid @RequestBody UserRequest request) {

        UserResponse response =
                this.userService.updateUserDetailsById(id, request);

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Delete a user",
            description = "Deletes an existing user account. " +
                    "This operation is available only to administrators."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only administrators can delete users"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(

            @Parameter(
                    description = "ID of the user to delete",
                    example = "7"
            )
            @PathVariable("id") Integer id) {

        this.userService.deleteUserById(id);

        return new ResponseEntity<>(HttpStatus.OK);
    }


    @Operation(
            summary = "Get user by ID",
            description = "Retrieves user details by user ID. " +
                    "A USER can retrieve only their own details, while an ADMIN " +
                    "can retrieve any user's details."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User details retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(

            @Parameter(
                    description = "ID of the user",
                    example = "7"
            )
            @PathVariable("id") Integer id) {

        UserResponse response =
                this.userService.getUserDetailsById(id);

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Get all users",
            description = "Retrieves details of all registered users. " +
                    "This operation is available only to administrators."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only administrators can retrieve all users"
            )
    })
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        List<UserResponse> allUsers =
                this.userService.getAllUsers();

        return ResponseEntity.ok(allUsers);
    }
}