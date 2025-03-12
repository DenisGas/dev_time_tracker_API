package com.example.DevTimeTracker_Api.repository;

import com.example.DevTimeTracker_Api.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, String> {
}
