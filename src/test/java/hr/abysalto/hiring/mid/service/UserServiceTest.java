package hr.abysalto.hiring.mid.service;

import hr.abysalto.hiring.mid.domain.User;
import hr.abysalto.hiring.mid.dto.request.RegisterRequest;
import hr.abysalto.hiring.mid.dto.response.UserResponse;
import hr.abysalto.hiring.mid.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .password("password123")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
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
    }

    @Test
    void shouldRegisterNewUser() {
        // Given
        // Stub the password encoder to prevent the NullPointerException
        // We tell it: "Whenever any string is passed to encode, just return 'encodedPassword'"
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("encodedPassword");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);

        // Ensure the mock user returned by save has the expected data
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        UserResponse response = userService.registerUser(registerRequest);

        // Then
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test", response.getFirstName());
        assertEquals("User", response.getLastName());

        // Verify interactions
        verify(passwordEncoder).encode(any(CharSequence.class)); // Verify encoding actually happened
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(registerRequest);
        });

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(registerRequest);
        });

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldFindUserByUsername() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.findByUsername("testuser");

        // Then
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void shouldReturnEmptyWhenUserNotFound() {
        // Given
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByUsername("unknown");

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findByUsername("unknown");
    }

    @Test
    void shouldVerifyPasswordCorrectly() {
        // Given
        String rawPassword = "password123";
        String encodedPassword = "mockedHashedPassword";

        // Tell the mock: "If you are asked to match these specific strings, return true"
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        // When
        boolean result = userService.verifyPassword(rawPassword, encodedPassword);

        // Then
        assertTrue(result);
        // Optional: verify the service actually reached out to the encoder
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    void shouldFailPasswordVerificationWithWrongPassword() {
        // Given
        String rawPassword = "wrongPassword";
        String encodedPassword = "mockedHashedPassword";

        // Tell the mock to return false for these inputs
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        // When
        boolean result = userService.verifyPassword(rawPassword, encodedPassword);

        // Then
        assertFalse(result, "Verification should fail when the password is incorrect");
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    void shouldMapUserToUserResponse() {
        // When
        UserResponse response = userService.mapToResponse(user);

        // Then
        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getFirstName(), response.getFirstName());
        assertEquals(user.getLastName(), response.getLastName());
        assertEquals(user.getCreatedAt(), response.getCreatedAt());
    }
}