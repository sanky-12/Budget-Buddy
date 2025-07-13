package com.budgetbuddy.auth_service.controller;

import com.budgetbuddy.auth_service.model.User;
import com.budgetbuddy.auth_service.security.JwtUtil;
import com.budgetbuddy.auth_service.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }


    @PostMapping("/register")
    public String register(@RequestBody User user){
        return authService.registerUser(user.getUsername(), user.getEmail(), user.getPassword());
    }

    @PostMapping("/login")
    public String login(@RequestBody User user){
        return authService.loginUser(user.getEmail(), user.getPassword());
    }

    @GetMapping("/profile")
    public User getProfile(HttpServletRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return authService.getUserByEmail(email);
    }


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
