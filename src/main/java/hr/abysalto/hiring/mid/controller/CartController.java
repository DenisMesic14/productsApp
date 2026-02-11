package hr.abysalto.hiring.mid.controller;

import hr.abysalto.hiring.mid.domain.User;
import hr.abysalto.hiring.mid.dto.request.AddToCartRequest;
import hr.abysalto.hiring.mid.dto.response.CartItemResponse;
import hr.abysalto.hiring.mid.dto.response.CartResponse;
import hr.abysalto.hiring.mid.service.CartService;
import hr.abysalto.hiring.mid.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cart", description = "Shopping cart management endpoints")
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    @Operation(summary = "Add product to cart")
    @PostMapping
    public ResponseEntity<CartItemResponse> addToCart(@RequestBody AddToCartRequest request) {
        Long userId = getCurrentUserId();
        CartItemResponse response = cartService.addToCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get current user's cart")
    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        Long userId = getCurrentUserId();
        CartResponse response = cartService.getUserCart(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Remove product from cart")
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long productId) {
        Long userId = getCurrentUserId();
        cartService.removeFromCart(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Clear entire cart")
    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        Long userId = getCurrentUserId();
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userService.findByUsername(username)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}