package hr.abysalto.hiring.mid.service;

import hr.abysalto.hiring.mid.domain.CartItem;
import hr.abysalto.hiring.mid.dto.request.AddToCartRequest;
import hr.abysalto.hiring.mid.dto.response.CartItemResponse;
import hr.abysalto.hiring.mid.dto.response.CartResponse;
import hr.abysalto.hiring.mid.dto.response.ProductResponse;
import hr.abysalto.hiring.mid.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    @Transactional
    public CartItemResponse addToCart(Long userId, AddToCartRequest request) {
        ProductResponse product = productService.getProductById(request.getProductId(), userId);

        Optional<CartItem> existingItem = cartItemRepository
                .findByUserIdAndProductId(userId, request.getProductId());

        CartItem cartItem;

        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        } else {
            cartItem = CartItem.builder()
                    .userId(userId)
                    .productId(request.getProductId())
                    .productTitle(product.getTitle())
                    .productPrice(product.getPrice())
                    .quantity(request.getQuantity())
                    .addedAt(LocalDateTime.now())
                    .build();
        }

        cartItem = cartItemRepository.save(cartItem);

        return mapToCartItemResponse(cartItem);
    }

    @Transactional
    public void removeFromCart(Long userId, Long productId) {
        if (cartItemRepository.findByUserIdAndProductId(userId, productId).isEmpty()) {
            throw new RuntimeException("Product not in cart");
        }

        cartItemRepository.deleteByUserIdAndProductId(userId, productId);
    }

    public CartResponse getUserCart(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);

        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());

        Integer totalItems = cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        Double totalPrice = itemResponses.stream()
                .mapToDouble(CartItemResponse::getSubtotal)
                .sum();

        return CartResponse.builder()
                .userId(userId)
                .items(itemResponses)
                .totalItems(totalItems)
                .totalPrice(totalPrice)
                .build();
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    private CartItemResponse mapToCartItemResponse(CartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productTitle(item.getProductTitle())
                .productPrice(item.getProductPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getProductPrice() * item.getQuantity())
                .addedAt(item.getAddedAt())
                .build();
    }
}