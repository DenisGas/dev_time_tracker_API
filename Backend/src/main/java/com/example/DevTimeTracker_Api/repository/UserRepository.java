package com.example.DevTimeTracker_Api.repository;

import com.example.DevTimeTracker_Api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
