package com.example.DevTimeTracker_Api.controller;

import com.example.DevTimeTracker_Api.dto.ErrorResponse;
import com.example.DevTimeTracker_Api.entity.DailyStats;
import com.example.DevTimeTracker_Api.entity.FileStats;
import com.example.DevTimeTracker_Api.entity.ProjectStats;
import com.example.DevTimeTracker_Api.repository.ProjectStatsRepository;
import com.example.DevTimeTracker_Api.utils.AuthUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/projects")
@Slf4j
@Tag(name = "Projects", description = "API for managing project statistics")
public class ProjectStatsController {

    @Autowired
    private ProjectStatsRepository projectStatsRepository;

    @PostMapping
    @Operation(summary = "Create a new project", description = "Creates a new project with aggregated daily stats")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectStats.class))),
            @ApiResponse(responseCode = "401", description = "You are not authorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> createProject(@RequestBody ProjectStats project) {
        log.info("Creating project with path: {}", project.getProjectPath());
        try {
            String userEmail = AuthUtils.getAuthenticatedUserEmail();
            project.setUserEmail(userEmail);

            if (project.getFiles() != null) {
                for (FileStats file : project.getFiles()) {
                    file.setProject(project);
                    if (file.getDailyStats() != null) {
                        for (DailyStats daily : file.getDailyStats()) {
                            daily.setFile(file);
                            daily.setProject(project);
                        }
                    }
                }
            }

            // Агрегация dailyStats из файлов
            if (project.getFiles() != null && !project.getFiles().isEmpty()) {
                project.setDailyStats(aggregateDailyStatsFromFiles(project.getFiles()));
            }

            ProjectStats savedProject = projectStatsRepository.save(project);
            log.info("Project created with ID: {}", savedProject.getId());
            return ResponseEntity.ok(savedProject);
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to create project with path: {}", project.getProjectPath(), e);
            return ResponseEntity.status(500).body(new ErrorResponse("Internal server error"));
        }
    }

    // Метод для агрегации dailyStats из файлов
    private List<DailyStats> aggregateDailyStatsFromFiles(List<FileStats> files) {
        Map<String, DailyStats> aggregatedStats = new HashMap<>();
        for (FileStats file : files) {
            if (file.getDailyStats() != null) {
                for (DailyStats daily : file.getDailyStats()) {
                    String date = daily.getDate();
                    aggregatedStats.compute(date, (key, existing) -> {
                        if (existing == null) {
                            DailyStats newDaily = new DailyStats();
                            newDaily.setDate(date);
                            newDaily.setCodingTime(daily.getCodingTime());
                            newDaily.setOpenTime(daily.getOpenTime());
                            return newDaily;
                        } else {
                            existing.setCodingTime(existing.getCodingTime() + daily.getCodingTime());
                            existing.setOpenTime(existing.getOpenTime() + daily.getOpenTime());
                            return existing;
                        }
                    });
                }
            }
        }
        return new ArrayList<>(aggregatedStats.values());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a project by ID", description = "Retrieves a project by its ID if it belongs to the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectStats.class))),
            @ApiResponse(responseCode = "401", description = "You are not authorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "You do not have access to this project",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getProject(@PathVariable Long id) {
        log.info("Fetching project with ID: {}", id);
        try {
            String userEmail = AuthUtils.getAuthenticatedUserEmail();
            Optional<ProjectStats> project = projectStatsRepository.findById(String.valueOf(id));
            if (project.isEmpty()) {
                log.warn("Project with ID {} not found", id);
                return ResponseEntity.status(404).body(new ErrorResponse("Project not found"));
            }
            if (!project.get().getUserEmail().equals(userEmail)) {
                log.warn("User {} does not have access to project ID {}", userEmail, id);
                return ResponseEntity.status(403).body(new ErrorResponse("You do not have access to this project"));
            }
            return ResponseEntity.ok(project.get());
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a project by ID", description = "Deletes a project by its ID if it belongs to the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project deleted",
                    content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "You are not authorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "You do not have access to this project",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> deleteProject(@PathVariable Long id) {
        log.info("Deleting project with ID: {}", id);
        try {
            String userEmail = AuthUtils.getAuthenticatedUserEmail();
            if (!projectStatsRepository.existsById(String.valueOf(id))) {
                log.warn("Project with ID {} not found", id);
                return ResponseEntity.status(404).body(new ErrorResponse("Project not found"));
            }
            ProjectStats project = projectStatsRepository.findById(String.valueOf(id)).get();
            if (!project.getUserEmail().equals(userEmail)) {
                log.warn("User {} does not have access to project ID {}", userEmail, id);
                return ResponseEntity.status(403).body(new ErrorResponse("You do not have access to this project"));
            }
            projectStatsRepository.deleteById(String.valueOf(id));
            log.info("Project with ID {} deleted", id);
            return ResponseEntity.ok("Project deleted");
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "Get all projects", description = "Retrieves all projects belonging to the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectStats.class))),
            @ApiResponse(responseCode = "401", description = "You are not authorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getAllProjects() {
        log.info("Fetching all projects");
        try {
            String userEmail = AuthUtils.getAuthenticatedUserEmail();
            List<ProjectStats> projects = projectStatsRepository.findByUserEmail(userEmail);
            log.info("Retrieved {} projects for user {}", projects.size(), userEmail);
            return ResponseEntity.ok(projects);
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(new ErrorResponse(e.getMessage()));
        }
    }
}