package com.example.DevTimeTracker_Api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.List;

@Entity
@Data
@ToString(exclude = {"files", "dailyStats", "gitHubBadge"}) // Исключаем коллекции и ссылки
public class ProjectStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String projectPath;
    private long totalCodingTime;
    private long totalOpenTime;

    private String userEmail;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("project-files")
    private List<FileStats> files;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("project-dailyStats")
    private List<DailyStats> dailyStats;

    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private GitHubBadge gitHubBadge;
}