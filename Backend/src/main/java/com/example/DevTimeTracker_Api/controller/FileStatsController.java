package com.example.DevTimeTracker_Api.controller;

import com.example.DevTimeTracker_Api.dto.ErrorResponse;
import com.example.DevTimeTracker_Api.entity.DailyStats;
import com.example.DevTimeTracker_Api.entity.FileStats;
import com.example.DevTimeTracker_Api.repository.FileStatsRepository;
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

import java.util.Optional;

@RestController
@RequestMapping("/api/files")
@Slf4j
@Tag(name = "Files", description = "API for managing file statistics")
public class FileStatsController {

    @Autowired
    private FileStatsRepository fileStatsRepository;

    @PostMapping
    @Operation(summary = "Create a new file", description = "Creates a new file with associated daily stats, linked to a project if specified")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileStats.class))),
            @ApiResponse(responseCode = "401", description = "You are not authorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "You do not have access to this project",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file data",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> createFile(@RequestBody FileStats file) {
        log.info("Creating file with path: {}", file.getFilePath());
        try {
            String userEmail = AuthUtils.getAuthenticatedUserEmail();

            // Валидация входных данных
            if (file.getFilePath() == null || file.getFilePath().isEmpty()) {
                log.warn("File path is missing");
                return ResponseEntity.badRequest().body(new ErrorResponse("File path cannot be empty"));
            }

            // Проверка доступа к проекту, если он указан
            if (file.getProject() != null) {
                if (file.getProject().getUserEmail() == null || !file.getProject().getUserEmail().equals(userEmail)) {
                    log.warn("User {} does not have access to project ID {}", userEmail, file.getProject().getId());
                    return ResponseEntity.status(403).body(new ErrorResponse("You do not have access to this project"));
                }
            }

            // Установка связей для dailyStats
            if (file.getDailyStats() != null) {
                for (DailyStats daily : file.getDailyStats()) {
                    daily.setFile(file);
                    if (file.getProject() != null) {
                        daily.setProject(file.getProject());
                    }
                }
            }

            FileStats savedFile = fileStatsRepository.save(file);
            log.info("File created with ID: {}", savedFile.getId());
            return ResponseEntity.ok(savedFile);
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to create file with path: {}", file.getFilePath(), e);
            return ResponseEntity.status(500).body(new ErrorResponse("Internal server error"));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a file by ID", description = "Retrieves a file by its ID if accessible to the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileStats.class))),
            @ApiResponse(responseCode = "401", description = "You are not authorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "You do not have access to this file",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "File not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getFile(@PathVariable Long id) {
        log.info("Fetching file with ID: {}", id);
        try {
            String userEmail = AuthUtils.getAuthenticatedUserEmail();
            Optional<FileStats> file = fileStatsRepository.findById(id);
            if (file.isEmpty()) {
                log.warn("File with ID {} not found", id);
                return ResponseEntity.status(404).body(new ErrorResponse("File not found"));
            }

            // Проверка доступа через проект, если файл привязан
            if (file.get().getProject() != null && !file.get().getProject().getUserEmail().equals(userEmail)) {
                log.warn("User {} does not have access to file ID {} (project ID: {})",
                        userEmail, id, file.get().getProject().getId());
                return ResponseEntity.status(403).body(new ErrorResponse("You do not have access to this file"));
            }

            return ResponseEntity.ok(file.get());
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a file by ID", description = "Deletes a file by its ID if accessible to the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File deleted",
                    content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "You are not authorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "You do not have access to this file",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "File not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> deleteFile(@PathVariable Long id) {
        log.info("Deleting file with ID: {}", id);
        try {
            String userEmail = AuthUtils.getAuthenticatedUserEmail();
            Optional<FileStats> file = fileStatsRepository.findById(id);
            if (file.isEmpty()) {
                log.warn("File with ID {} not found", id);
                return ResponseEntity.status(404).body(new ErrorResponse("File not found"));
            }

            // Проверка доступа через проект
            if (file.get().getProject() != null && !file.get().getProject().getUserEmail().equals(userEmail)) {
                log.warn("User {} does not have access to file ID {} (project ID: {})",
                        userEmail, id, file.get().getProject().getId());
                return ResponseEntity.status(403).body(new ErrorResponse("You do not have access to this file"));
            }

            fileStatsRepository.deleteById(id);
            log.info("File with ID {} deleted", id);
            return ResponseEntity.ok("File deleted");
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(new ErrorResponse(e.getMessage()));
        }
    }
}