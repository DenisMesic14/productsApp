package hr.abysalto.hiring.mid.controller;

import hr.abysalto.hiring.mid.domain.User;
import hr.abysalto.hiring.mid.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Test", description = "Test endpoints to verify setup")
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "Get all users from database")
    @GetMapping("/users")
    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Operation(summary = "Database health check")
    @GetMapping("/health")
    public String healthCheck() {
        long userCount = userRepository.count();
        return "Database is working! Users in database: " + userCount;
    }
}