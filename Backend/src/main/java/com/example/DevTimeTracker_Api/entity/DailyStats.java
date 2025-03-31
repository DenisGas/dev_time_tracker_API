package com.example.DevTimeTracker_Api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Data
@ToString(exclude = {"project", "file"}) // Исключаем ссылки на ProjectStats и FileStats
public class DailyStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String date;
    private long codingTime;
    private long openTime;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = true)
    @JsonBackReference("project-dailyStats")
    private ProjectStats project;

    @ManyToOne
    @JoinColumn(name = "file_id", nullable = true)
    @JsonBackReference("file-dailyStats")
    private FileStats file;
}