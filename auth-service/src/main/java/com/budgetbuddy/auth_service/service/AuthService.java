package com.budgetbuddy.auth_service.service;

import com.budgetbuddy.auth_service.exception.EmailAlreadyUsedException;
import com.budgetbuddy.auth_service.exception.InvalidCredentialsException;
import com.budgetbuddy.auth_service.exception.UserNotFoundException;
import com.budgetbuddy.auth_service.model.User;
import com.budgetbuddy.auth_service.repository.UserRepository;
import com.budgetbuddy.auth_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String registerUser(String username, String email, String password){
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException();
        }

        User user = new User(null, username, email, passwordEncoder.encode(password));
        userRepository.save(user);

        return "User Registered Successfully";
    }

    public String loginUser(String email, String password){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + email));

        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new InvalidCredentialsException();
        }

        return jwtUtil.generateToken(user.getId() ,email);
    }

    public User getUserProfile(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public String updateUserProfile(String userId, String newUsername, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setUsername(newUsername);
        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }
        userRepository.save(user);
        return "Profile updated successfully";
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }


}
