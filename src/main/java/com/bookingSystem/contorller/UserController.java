package com.bookingSystem.contorller;

import com.bookingSystem.dto.UserRequest;
import com.bookingSystem.dto.UserResponse;
import com.bookingSystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/booking/users")
@RequiredArgsConstructor
public class UserController {
    public static final Logger LOGGER = Logger.getLogger(UserController.class.getName());
    private final UserService userService;

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUserDetails(@PathVariable("id") Integer id, @Valid @RequestBody UserRequest request){
        LOGGER.info("Received request to update user with id: " + id);
        UserResponse response = this.userService.updateUserDetailsById(id, request);
        LOGGER.info("Successfully updated existing user: " + response.toString());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Integer id){
        LOGGER.info("Received request to delete user with id: " + id);
        this.userService.deleteUserById(id);
        LOGGER.info("Successfully deleted user with id: " + id);
        return ResponseEntity.ok("Deleted User with id: " + id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") Integer id){
        LOGGER.info("Received request to get user by id: " + id);
        UserResponse response = this.userService.getUserDetailsById(id);
        LOGGER.info("Successfully sent user with details: " + response.toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(){
        LOGGER.info("Received request to get all users");
        List<UserResponse> allUsers = this.userService.getAllUsers();
        LOGGER.info("Successfully sent all users");
        return ResponseEntity.ok(allUsers);
    }
}
