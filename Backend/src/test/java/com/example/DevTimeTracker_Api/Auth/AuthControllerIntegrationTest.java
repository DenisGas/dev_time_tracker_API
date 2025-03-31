package com.example.DevTimeTracker_Api.Auth;

import com.example.DevTimeTracker_Api.repository.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private static final String EMAIL_BASE = "test@example.com";

    private String generateUniqueEmail() {
        // Проверяем, существует ли базовый email
        if (userRepository.findByEmail(EMAIL_BASE).isEmpty()) {
            return EMAIL_BASE;
        }

        // Генерируем 8 случайных букв
        String randomPart = RandomStringUtils.randomAlphabetic(8);

        // Собираем новый email: test+random8@example.com
        return "test+" + randomPart + "@example.com";
    }

    @Test
    public void testRegisterAndLogin() throws Exception {
        String uniqueEmail = generateUniqueEmail();

        // Регистрация
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"" + uniqueEmail + "\", \"password\": \"123\", \"username\": \"TestUser\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered"));

        // Логин
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"" + uniqueEmail + "\", \"password\": \"123\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.not(Matchers.emptyString()))); // Проверяем, что токен возвращён
    }
}