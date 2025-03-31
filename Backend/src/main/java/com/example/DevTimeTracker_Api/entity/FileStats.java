package com.example.DevTimeTracker_Api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.util.List;

@Entity
@Data
@ToString(exclude = {"project", "dailyStats"}) // Исключаем ссылки на ProjectStats и DailyStats
public class FileStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filePath;
    private long openTime;
    private long codingTime;
    private String type;

    @ManyToOne
    @JoinColumn(name = "project_id")
    @JsonBackReference("project-files")
    private ProjectStats project;

    @OneToMany(mappedBy = "file", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("file-dailyStats")
    private List<DailyStats> dailyStats;
}