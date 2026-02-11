package hr.abysalto.hiring.mid.service;

import hr.abysalto.hiring.mid.domain.User;
import hr.abysalto.hiring.mid.dto.request.LoginRequest;
import hr.abysalto.hiring.mid.dto.response.AuthResponse;
import hr.abysalto.hiring.mid.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;

    public AuthResponse login(LoginRequest request) {

        Optional<User> userOpt = userService.findByUsername(request.getUsername());

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Invalid username or password");
        }

        User user = userOpt.get();

        if (!userService.verifyPassword(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        if (!user.isEnabled()) {
            throw new RuntimeException("User account is disabled");
        }

        UserResponse userResponse = userService.mapToResponse(user);

        return AuthResponse.builder()
                .message("Login successful")
                .user(userResponse)
                .build();
    }
}