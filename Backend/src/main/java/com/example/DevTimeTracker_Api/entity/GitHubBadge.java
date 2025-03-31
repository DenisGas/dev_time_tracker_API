package com.example.DevTimeTracker_Api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class GitHubBadge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean isPublic;
    private long totalCodingTime;

    @OneToOne
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectStats project;
}
