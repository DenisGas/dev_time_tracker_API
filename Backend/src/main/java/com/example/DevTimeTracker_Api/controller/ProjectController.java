package com.example.DevTimeTracker_Api.controller;

import com.example.DevTimeTracker_Api.entity.Project;
import com.example.DevTimeTracker_Api.entity.User;
import com.example.DevTimeTracker_Api.repository.ProjectRepository;
import com.example.DevTimeTracker_Api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<Project> saveProject(@RequestBody Project project, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = (User) userService.loadUserByUsername(userDetails.getUsername());
        project.setUser(user);
        return ResponseEntity.ok(projectRepository.save(project));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProject(@PathVariable String id, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        if (!project.getUser().getUsername().equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(project);
    }
}