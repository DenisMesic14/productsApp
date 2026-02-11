package hr.abysalto.hiring.mid.controller;

import hr.abysalto.hiring.mid.domain.User;
import hr.abysalto.hiring.mid.dto.response.ProductListResponse;
import hr.abysalto.hiring.mid.dto.response.ProductResponse;
import hr.abysalto.hiring.mid.service.ProductService;
import hr.abysalto.hiring.mid.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Products", description = "Product browsing endpoints")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final UserService userService;

    @Operation(summary = "Get all products with pagination")
    @GetMapping
    public ResponseEntity<ProductListResponse> getAllProducts(
            @Parameter(description = "Number of items to return") @RequestParam(defaultValue = "10") Integer limit,
            @Parameter(description = "Number of items to skip") @RequestParam(defaultValue = "0") Integer skip,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String order) {

        Long userId = getCurrentUserId();
        ProductListResponse response = productService.getAllProducts(limit, skip, sortBy, order, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get single product by ID")
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long productId) {
        Long userId = getCurrentUserId();
        ProductResponse response = productService.getProductById(productId, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Search products by query")
    @GetMapping("/search")
    public ResponseEntity<ProductListResponse> searchProducts(
            @Parameter(description = "Search query") @RequestParam String q) {

        Long userId = getCurrentUserId();
        ProductListResponse response = productService.searchProducts(q, userId);
        return ResponseEntity.ok(response);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userService.findByUsername(username)
                .map(User::getId)
                .orElse(null);
    }
}