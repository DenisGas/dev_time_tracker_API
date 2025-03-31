package com.example.DevTimeTracker_Api.controller;

import com.example.DevTimeTracker_Api.dto.ErrorResponse;
import com.example.DevTimeTracker_Api.entity.User;
import com.example.DevTimeTracker_Api.repository.UserRepository;
import com.example.DevTimeTracker_Api.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a new user and stores their credentials")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid user data",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "User with this email already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> register(@RequestBody User user) {
        log.info("Registering user with email: {}", user.getUsername());
        try {
            if (user.getUsername() == null || user.getPassword() == null) {
                log.warn("Email or password is missing for registration");
                return ResponseEntity.badRequest().body(new ErrorResponse("Email and password are required"));
            }

            if (userRepository.findByEmail(user.getUsername()).isPresent()) {
                log.warn("User with email {} already exists", user.getUsername());
                return ResponseEntity.status(409).body(new ErrorResponse("User with this email already exists"));
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            log.info("User {} registered successfully", user.getUsername());
            return ResponseEntity.ok("User registered");
        } catch (Exception e) {
            log.error("Failed to register user with email: {}", user.getUsername(), e);
            return ResponseEntity.status(500).body(new ErrorResponse("Internal server error"));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates a user and returns a JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "JWT token returned",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid email or password",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> login(@RequestBody User user) {
        log.info("Login attempt for email: {}", user.getUsername());
        try {
            if (user.getUsername() == null || user.getPassword() == null) {
                log.warn("Email or password is missing for login");
                return ResponseEntity.badRequest().body(new ErrorResponse("Email and password are required"));
            }

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            String token = jwtUtil.generateToken(userDetails);
            log.info("User {} logged in successfully", user.getUsername());
            return ResponseEntity.ok(token);
        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for email: {}", user.getUsername());
            return ResponseEntity.status(401).body(new ErrorResponse("Invalid email or password"));
        } catch (Exception e) {
            log.error("Login failed for email: {}", user.getUsername(), e);
            return ResponseEntity.status(500).body(new ErrorResponse("Internal server error"));
        }
    }
}