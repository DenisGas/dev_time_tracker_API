package com.example.DevTimeTracker_Api.repository;

import com.example.DevTimeTracker_Api.entity.DailyStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyStatsRepository extends JpaRepository<DailyStats, Long> {
}
