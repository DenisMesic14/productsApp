package hr.abysalto.hiring.mid.service;

import hr.abysalto.hiring.mid.domain.User;
import hr.abysalto.hiring.mid.dto.request.LoginRequest;
import hr.abysalto.hiring.mid.dto.response.AuthResponse;
import hr.abysalto.hiring.mid.dto.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthService authService;

    private LoginRequest loginRequest;
    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        user = User.builder()
                .id(1L)
                .username("testuser")
                .password("$2a$10$hashedPassword")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Test
    void shouldLoginSuccessfully() {
        // Given
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userService.verifyPassword("password123", user.getPassword())).thenReturn(true);
        when(userService.mapToResponse(user)).thenReturn(userResponse);

        // When
        AuthResponse response = authService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals("Login successful", response.getMessage());
        assertNotNull(response.getUser());
        assertEquals("testuser", response.getUser().getUsername());

        verify(userService).findByUsername("testuser");
        verify(userService).verifyPassword("password123", user.getPassword());
        verify(userService).mapToResponse(user);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        String username = "unknown";
        // Ensure the stub uses the same username the loginRequest will use
        when(userService.findByUsername(username)).thenReturn(Optional.empty());

        // Assuming loginRequest.getUsername() returns "unknown"
        loginRequest.setUsername(username);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid username or password", exception.getMessage());

        // Verify using the SAME username defined in Given
        verify(userService).findByUsername(username);
        // Ensure we stopped early and never tried to verify a password
        verify(userService, never()).verifyPassword(anyString(), anyString());
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsIncorrect() {
        // Given
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userService.verifyPassword("password123", user.getPassword())).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid username or password", exception.getMessage());
        verify(userService).findByUsername("testuser");
        verify(userService).verifyPassword("password123", user.getPassword());
        verify(userService, never()).mapToResponse(any());
    }

    @Test
    void shouldThrowExceptionWhenUserIsDisabled() {
        // Given
        user.setEnabled(false);
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userService.verifyPassword("password123", user.getPassword())).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("User account is disabled", exception.getMessage());
        verify(userService).findByUsername("testuser");
        verify(userService).verifyPassword("password123", user.getPassword());
        verify(userService, never()).mapToResponse(any());
    }
}