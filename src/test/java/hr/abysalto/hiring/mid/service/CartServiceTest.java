package hr.abysalto.hiring.mid.service;

import hr.abysalto.hiring.mid.domain.CartItem;
import hr.abysalto.hiring.mid.dto.request.AddToCartRequest;
import hr.abysalto.hiring.mid.dto.response.CartItemResponse;
import hr.abysalto.hiring.mid.dto.response.CartResponse;
import hr.abysalto.hiring.mid.dto.response.ProductResponse;
import hr.abysalto.hiring.mid.repository.CartItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private CartService cartService;

    private ProductResponse productResponse;
    private CartItem cartItem;
    private AddToCartRequest addToCartRequest;

    @BeforeEach
    void setUp() {
        productResponse = ProductResponse.builder()
                .id(5L)
                .title("iPhone 15")
                .price(999.99)
                .stock(50)
                .build();

        cartItem = CartItem.builder()
                .id(1L)
                .userId(1L)
                .productId(5L)
                .productTitle("iPhone 15")
                .productPrice(999.99)
                .quantity(2)
                .addedAt(LocalDateTime.now())
                .build();

        addToCartRequest = AddToCartRequest.builder()
                .productId(5L)
                .quantity(2)
                .build();
    }

    @Test
    void shouldAddNewProductToCart() {
        // Given
        when(productService.getProductById(5L, 1L)).thenReturn(productResponse);
        when(cartItemRepository.findByUserIdAndProductId(1L, 5L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

        // When
        CartItemResponse response = cartService.addToCart(1L, addToCartRequest);

        // Then
        assertNotNull(response);
        assertEquals(5L, response.getProductId());
        assertEquals("iPhone 15", response.getProductTitle());
        assertEquals(999.99, response.getProductPrice());
        assertEquals(2, response.getQuantity());
        assertEquals(1999.98, response.getSubtotal());

        verify(productService).getProductById(5L, 1L);
        verify(cartItemRepository).findByUserIdAndProductId(1L, 5L);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void shouldUpdateQuantityWhenProductAlreadyInCart() {
        // Given
        CartItem existingItem = CartItem.builder()
                .id(1L)
                .userId(1L)
                .productId(5L)
                .productTitle("iPhone 15")
                .productPrice(999.99)
                .quantity(2)
                .addedAt(LocalDateTime.now())
                .build();

        when(productService.getProductById(5L, 1L)).thenReturn(productResponse);
        when(cartItemRepository.findByUserIdAndProductId(1L, 5L))
                .thenReturn(Optional.of(existingItem));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddToCartRequest request = AddToCartRequest.builder()
                .productId(5L)
                .quantity(3)
                .build();

        // When
        CartItemResponse response = cartService.addToCart(1L, request);

        // Then
        assertNotNull(response);
        assertEquals(5, response.getQuantity()); // 2 + 3 = 5
        assertEquals(4999.95, response.getSubtotal(), 0.01); // 999.99 × 5

        verify(productService).getProductById(5L, 1L);
        verify(cartItemRepository).findByUserIdAndProductId(1L, 5L);
        verify(cartItemRepository).save(argThat(item -> item.getQuantity() == 5));
    }

    @Test
    void shouldRemoveProductFromCart() {
        // Given
        when(cartItemRepository.findByUserIdAndProductId(1L, 5L))
                .thenReturn(Optional.of(cartItem));
        doNothing().when(cartItemRepository).deleteByUserIdAndProductId(1L, 5L);

        // When
        cartService.removeFromCart(1L, 5L);

        // Then
        verify(cartItemRepository).findByUserIdAndProductId(1L, 5L);
        verify(cartItemRepository).deleteByUserIdAndProductId(1L, 5L);
    }

    @Test
    void shouldThrowExceptionWhenRemovingNonExistentCartItem() {
        // Given
        when(cartItemRepository.findByUserIdAndProductId(1L, 5L))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.removeFromCart(1L, 5L);
        });

        assertEquals("Product not in cart", exception.getMessage());
        verify(cartItemRepository).findByUserIdAndProductId(1L, 5L);
        verify(cartItemRepository, never()).deleteByUserIdAndProductId(any(), any());
    }

    @Test
    void shouldGetUserCartWithCorrectTotals() {
        // Given
        CartItem item1 = CartItem.builder()
                .id(1L)
                .userId(1L)
                .productId(5L)
                .productTitle("iPhone 15")
                .productPrice(999.99)
                .quantity(2)
                .addedAt(LocalDateTime.now())
                .build();

        CartItem item2 = CartItem.builder()
                .id(2L)
                .userId(1L)
                .productId(12L)
                .productTitle("Samsung S24")
                .productPrice(899.99)
                .quantity(1)
                .addedAt(LocalDateTime.now())
                .build();

        when(cartItemRepository.findByUserId(1L)).thenReturn(Arrays.asList(item1, item2));

        // When
        CartResponse response = cartService.getUserCart(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals(2, response.getItems().size());
        assertEquals(3, response.getTotalItems()); // 2 + 1 = 3
        assertEquals(2899.97, response.getTotalPrice(), 0.01); // 1999.98 + 899.99

        CartItemResponse firstItem = response.getItems().get(0);
        assertEquals(1999.98, firstItem.getSubtotal(), 0.01);

        CartItemResponse secondItem = response.getItems().get(1);
        assertEquals(899.99, secondItem.getSubtotal(), 0.01);

        verify(cartItemRepository).findByUserId(1L);
    }

    @Test
    void shouldReturnEmptyCartWhenUserHasNoItems() {
        // Given
        when(cartItemRepository.findByUserId(1L)).thenReturn(Arrays.asList());

        // When
        CartResponse response = cartService.getUserCart(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertTrue(response.getItems().isEmpty());
        assertEquals(0, response.getTotalItems());
        assertEquals(0.0, response.getTotalPrice());

        verify(cartItemRepository).findByUserId(1L);
    }

    @Test
    void shouldClearCart() {
        // Given
        doNothing().when(cartItemRepository).deleteByUserId(1L);

        // When
        cartService.clearCart(1L);

        // Then
        verify(cartItemRepository).deleteByUserId(1L);
    }
}