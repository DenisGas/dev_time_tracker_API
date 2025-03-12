package com.example.DevTimeTracker_Api.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@Data
public class Device{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String deviceName; // Например, "Laptop-Win", "Desktop-Linux"
    private String ide; // Например, "VS Code", "IntelliJ"

    @ManyToOne
    @JoinColumn(name = "username", referencedColumnName = "username") // Указываем явно
    private User user;
}