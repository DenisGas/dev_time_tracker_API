package com.example.DevTimeTracker_Api.repository;

import com.example.DevTimeTracker_Api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}

