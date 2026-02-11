package hr.abysalto.hiring.mid.controller;

import hr.abysalto.hiring.mid.domain.User;
import hr.abysalto.hiring.mid.service.FavoriteService;
import hr.abysalto.hiring.mid.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Favorites", description = "Favorite products management endpoints")
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final UserService userService;

    @Operation(summary = "Add product to favorites")
    @PostMapping("/{productId}")
    public ResponseEntity<Void> addToFavorites(@PathVariable Long productId) {
        Long userId = getCurrentUserId();
        favoriteService.addToFavorites(userId, productId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Remove product from favorites")
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeFromFavorites(@PathVariable Long productId) {
        Long userId = getCurrentUserId();
        favoriteService.removeFromFavorites(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get user's favorite product IDs")
    @GetMapping
    public ResponseEntity<List<Long>> getFavorites() {
        Long userId = getCurrentUserId();
        List<Long> favoriteIds = favoriteService.getUserFavoriteProductIds(userId);
        return ResponseEntity.ok(favoriteIds);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userService.findByUsername(username)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}