package com.budgetbuddy.auth_service.controller;

import com.budgetbuddy.auth_service.model.User;
import com.budgetbuddy.auth_service.security.JwtUtil;
import com.budgetbuddy.auth_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }


    @Operation(
            summary     = "Register a new user",
            description = "Creates an account with username, email, and password",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User registered",
                            content = @Content(mediaType = "text/plain",
                                    schema = @Schema(implementation = String.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Email already in use")
            }
    )
    @PostMapping("/register")
    public String register(
            @Parameter(description = "User object with username, email, password", required = true)
            @RequestBody User user){
        return authService.registerUser(user.getUsername(), user.getEmail(), user.getPassword());
    }


    @Operation(
            summary     = "User login",
            description = "Authenticates and returns a JWT token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "JWT token",
                            content = @Content(mediaType = "text/plain",
                                    schema = @Schema(implementation = String.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials")
            }
    )
    @PostMapping("/login")
    public String login(
            @Parameter(description = "User object with email and password", required = true)
            @RequestBody User user){
        return authService.loginUser(user.getEmail(), user.getPassword());
    }


    @Operation(
            summary     = "Get current user profile",
            description = "Returns the authenticated user’s details",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User profile",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = User.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @GetMapping("/profile")
    public User getProfile(HttpServletRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return authService.getUserByEmail(email);
    }


    @Operation(
            summary     = "Update current user profile",
            description = "Change username and/or password",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Profile updated",
                            content = @Content(mediaType = "text/plain",
                                    schema = @Schema(implementation = String.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @PutMapping("/profile")
    public String updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody User updatedUser
    ) {
        String userId = extractUserIdFromHeader(authHeader);
        return authService.updateUserProfile(userId, updatedUser.getUsername(), updatedUser.getPassword());
    }

    private String extractUserIdFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7); // Remove "Bearer "
        return jwtUtil.extractUserId(token);
    }
}
