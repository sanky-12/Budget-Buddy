package com.budgetbuddy.auth_service.service;

import com.budgetbuddy.auth_service.exception.EmailAlreadyUsedException;
import com.budgetbuddy.auth_service.exception.InvalidCredentialsException;
import com.budgetbuddy.auth_service.exception.UserNotFoundException;
import com.budgetbuddy.auth_service.model.User;
import com.budgetbuddy.auth_service.repository.UserRepository;
import com.budgetbuddy.auth_service.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private final String RAW_PW   = "plain";
    private final String ENC_PW   = "encoded";
    private final String EMAIL    = "a@x.com";
    private final String USER_ID  = "user-123";
    private final User   EXISTING = new User(USER_ID, "bob", EMAIL, ENC_PW);

    @BeforeEach
    void setup() {
        // no-op
    }

    @Test
    void registerUser_whenEmailNotUsed_savesAndReturnsMessage() {
        when(userRepo.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(RAW_PW)).thenReturn(ENC_PW);

        String msg = authService.registerUser("bob", EMAIL, RAW_PW);

        assertThat(msg).isEqualTo("User Registered Successfully");
        // Verify save called with encoded password
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("bob");
        assertThat(saved.getEmail()).isEqualTo(EMAIL);
        assertThat(saved.getPassword()).isEqualTo(ENC_PW);
    }

    @Test
    void registerUser_whenEmailUsed_throwsEmailAlreadyUsed() {
        when(userRepo.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() ->
                authService.registerUser("bob", EMAIL, RAW_PW)
        ).isInstanceOf(EmailAlreadyUsedException.class);

        verify(userRepo, never()).save(any());
    }

    @Test
    void loginUser_withValidCredentials_returnsJwt() {
        when(userRepo.findByEmail(EMAIL)).thenReturn(Optional.of(EXISTING));
        when(passwordEncoder.matches(RAW_PW, ENC_PW)).thenReturn(true);
        when(jwtUtil.generateToken(USER_ID, EMAIL)).thenReturn("token-abc");

        String token = authService.loginUser(EMAIL, RAW_PW);

        assertThat(token).isEqualTo("token-abc");
    }

    @Test
    void loginUser_whenUserNotFound_throwsUserNotFound() {
        when(userRepo.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authService.loginUser(EMAIL, RAW_PW)
        ).isInstanceOf(UserNotFoundException.class);

        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtUtil, never()).generateToken(any(), any());
    }


    @Test
    void loginUser_whenPasswordMismatch_throwsInvalidCredentials() {
        when(userRepo.findByEmail(EMAIL)).thenReturn(Optional.of(EXISTING));
        when(passwordEncoder.matches("wrong", ENC_PW)).thenReturn(false);

        assertThatThrownBy(() ->
                authService.loginUser(EMAIL, "wrong")
        ).isInstanceOf(InvalidCredentialsException.class);

        verify(jwtUtil, never()).generateToken(any(), any());
    }


    @Test
    void getUserProfile_existingUser_returnsUser() {
        when(userRepo.findById(USER_ID)).thenReturn(Optional.of(EXISTING));

        User u = authService.getUserProfile(USER_ID);

        assertThat(u).isSameAs(EXISTING);
    }


    @Test
    void getUserProfile_missingUser_throwsUserNotFound() {
        when(userRepo.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authService.getUserProfile(USER_ID)
        ).isInstanceOf(UserNotFoundException.class);
    }


    @Test
    void updateUserProfile_updatesUsernameAndPassword() {
        when(userRepo.findById(USER_ID)).thenReturn(Optional.of(EXISTING));
        when(passwordEncoder.encode("newPw")).thenReturn("encNew");

        String msg = authService.updateUserProfile(USER_ID, "alice", "newPw");
        assertThat(msg).isEqualTo("Profile updated successfully");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("alice");
        assertThat(saved.getPassword()).isEqualTo("encNew");
    }


    @Test
    void updateUserProfile_withoutNewPassword_onlyUpdatesUsername() {
        when(userRepo.findById(USER_ID)).thenReturn(Optional.of(EXISTING));

        authService.updateUserProfile(USER_ID, "alice", null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo(ENC_PW);
    }


    @Test
    void updateUserProfile_missingUser_throwsUserNotFound() {
        when(userRepo.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authService.updateUserProfile(USER_ID, "x", "y")
        ).isInstanceOf(UserNotFoundException.class);
    }


    @Test
    void getUserByEmail_existing_returnsUser() {
        when(userRepo.findByEmail(EMAIL)).thenReturn(Optional.of(EXISTING));
        assertThat(authService.getUserByEmail(EMAIL)).isSameAs(EXISTING);
    }

    @Test
    void getUserByEmail_missing_throwsRuntime() {
        when(userRepo.findByEmail(EMAIL)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.getUserByEmail(EMAIL))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

}
