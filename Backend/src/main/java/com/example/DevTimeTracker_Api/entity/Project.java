package com.example.DevTimeTracker_Api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Project {
    @Id
    private String id;
    private String projectPath;
    private long totalCodingTime;
    private long totalOpenTime;

    @ManyToOne
    @JoinColumn(name = "username", referencedColumnName = "username") // Указываем явно
    private User user;
}
