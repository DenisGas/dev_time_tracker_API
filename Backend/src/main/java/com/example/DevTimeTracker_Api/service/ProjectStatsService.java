package com.example.DevTimeTracker_Api.service;

import com.example.DevTimeTracker_Api.entity.ProjectStats;
import com.example.DevTimeTracker_Api.repository.ProjectStatsRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProjectStatsService {
    private final ProjectStatsRepository projectStatsRepository;

    public ProjectStatsService(ProjectStatsRepository projectStatsRepository) {
        this.projectStatsRepository = projectStatsRepository;
    }

    public List<ProjectStats> getAllProjects() {
        return projectStatsRepository.findAll();
    }

    public ProjectStats saveProject(ProjectStats project) {
        return projectStatsRepository.save(project);
    }
}
