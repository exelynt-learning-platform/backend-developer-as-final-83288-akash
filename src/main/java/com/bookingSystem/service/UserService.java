package com.bookingSystem.service;

import com.bookingSystem.dto.RegisterRequest;
import com.bookingSystem.dto.UserRequest;
import com.bookingSystem.dto.UserResponse;
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
}
