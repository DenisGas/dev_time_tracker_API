package com.example.DevTimeTracker_Api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deviceName; // Например, "Laptop-Win", "Desktop-Linux"
    private String ide; // Например, "VS Code", "IntelliJ"

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
