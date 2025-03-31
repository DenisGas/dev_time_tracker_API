package com.example.DevTimeTracker_Api;

import com.example.DevTimeTracker_Api.entity.FileStats;
import com.example.DevTimeTracker_Api.entity.ProjectStats;
import com.example.DevTimeTracker_Api.entity.User;
import com.example.DevTimeTracker_Api.repository.FileStatsRepository;
import com.example.DevTimeTracker_Api.repository.ProjectStatsRepository;
import com.example.DevTimeTracker_Api.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectStatsRepository projectStatsRepository;

    @Autowired
    private FileStatsRepository fileStatsRepository;

    private String jwtToken;

    @BeforeEach
    public void setup() throws Exception {
        // Очистка базы перед тестом
        cleanup();

        // Регистрация пользователя
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered"));

        // Логин и получение токена
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        jwtToken = loginResponse;
        System.out.println("JWT Token: " + jwtToken);
    }

    @AfterEach
    public void cleanup() {
        // Очистка данных после каждого теста
        fileStatsRepository.deleteAll();
        projectStatsRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void testRegisterDuplicateUser() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User with this email already exists"));
    }

    @Test
    public void testLoginInvalidCredentials() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("wrong");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    public void testCreateProject() throws Exception {
        ProjectStats project = new ProjectStats();
        project.setProjectPath("/path/to/project");

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectPath").value("/path/to/project"))
                .andExpect(jsonPath("$.userEmail").value("test@example.com"));
    }

    @Test
    public void testGetProject() throws Exception {
        ProjectStats project = new ProjectStats();
        project.setProjectPath("/path/to/project");

        String response = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ProjectStats createdProject = objectMapper.readValue(response, ProjectStats.class);

        mockMvc.perform(get("/api/projects/" + createdProject.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdProject.getId()))
                .andExpect(jsonPath("$.projectPath").value("/path/to/project"));
    }

    @Test
    public void testDeleteProject() throws Exception {
        ProjectStats project = new ProjectStats();
        project.setProjectPath("/path/to/project");

        String response = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ProjectStats createdProject = objectMapper.readValue(response, ProjectStats.class);

        mockMvc.perform(delete("/api/projects/" + createdProject.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Project deleted"));
    }

    @Test
    public void testGetAllProjects() throws Exception {
        ProjectStats project = new ProjectStats();
        project.setProjectPath("/path/to/project");

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/projects")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].projectPath").value("/path/to/project"));
    }

    @Test
    public void testCreateFile() throws Exception {
        FileStats file = new FileStats();
        file.setFilePath("/path/to/file");

        mockMvc.perform(post("/api/files")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(file)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filePath").value("/path/to/file"));
    }

    @Test
    public void testGetFile() throws Exception {
        FileStats file = new FileStats();
        file.setFilePath("/path/to/file");

        String response = mockMvc.perform(post("/api/files")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(file)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        FileStats createdFile = objectMapper.readValue(response, FileStats.class);

        mockMvc.perform(get("/api/files/" + createdFile.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdFile.getId()))
                .andExpect(jsonPath("$.filePath").value("/path/to/file"));
    }

    @Test
    public void testDeleteFile() throws Exception {
        FileStats file = new FileStats();
        file.setFilePath("/path/to/file");

        String response = mockMvc.perform(post("/api/files")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(file)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        FileStats createdFile = objectMapper.readValue(response, FileStats.class);

        mockMvc.perform(delete("/api/files/" + createdFile.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().string("File deleted"));
    }

    @Test
    public void testAccessDeniedForOtherUserProject() throws Exception {
        ProjectStats project = new ProjectStats();
        project.setProjectPath("/path/to/project");

        String projectResponse = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ProjectStats createdProject = objectMapper.readValue(projectResponse, ProjectStats.class);

        User user2 = new User();
        user2.setEmail("other@example.com");
        user2.setPassword("456");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isOk());

        String token2 = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(get("/api/projects/" + createdProject.getId())
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have access to this project"));
    }
}