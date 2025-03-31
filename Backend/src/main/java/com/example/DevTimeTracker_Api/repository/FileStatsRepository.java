package com.example.DevTimeTracker_Api.repository;

import com.example.DevTimeTracker_Api.entity.FileStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileStatsRepository extends JpaRepository<FileStats, Long> {
}
