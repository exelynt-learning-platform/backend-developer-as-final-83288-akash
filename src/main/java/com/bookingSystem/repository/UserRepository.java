package com.bookingSystem.repository;

import com.bookingSystem.entity.User;
import com.bookingSystem.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String username);

    @Query("SELECT u FROM User u WHERE u.role = :role")
    Optional<User> findByRole(@Param("role") UserRole role);

    boolean existsByEmail(String email);
}
