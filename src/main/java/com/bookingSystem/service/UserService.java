package com.bookingSystem.service;

import com.bookingSystem.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {
    UserResponse addUser(RegisterRequest request);
    void deleteUserById(Integer id);
    UserResponse getUserDetailsById(Integer id);
    UserResponse updateUserDetailsById(Integer id, UserRequest request);
    List<UserResponse> getAllUsers();
    UserResponse getUserByEmail(String email);

    LoginResponse loginUser(LoginRequest request);
}
