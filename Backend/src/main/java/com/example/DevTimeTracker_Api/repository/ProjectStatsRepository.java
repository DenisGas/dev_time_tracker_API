package com.example.DevTimeTracker_Api.repository;

import com.example.DevTimeTracker_Api.entity.ProjectStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectStatsRepository extends JpaRepository<ProjectStats, String> {
    List<ProjectStats> findByUserEmail(String userEmail);
}
