package hr.abysalto.hiring.mid.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.abysalto.hiring.mid.dto.response.ProductListResponse;
import hr.abysalto.hiring.mid.dto.response.ProductResponse;
import hr.abysalto.hiring.mid.repository.FavoriteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private RestTemplate restTemplate;

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private ProductService productService;

    private String mockDummyJsonResponse;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(productService, "dummyJsonBaseUrl", "https://dummyjson.com");
        ReflectionTestUtils.setField(productService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(productService, "objectMapper", objectMapper);

        mockDummyJsonResponse = """
                {
                  "products": [
                    {
                      "id": 1,
                      "title": "iPhone 9",
                      "description": "An apple mobile",
                      "price": 549,
                      "discountPercentage": 12.96,
                      "rating": 4.69,
                      "stock": 94,
                      "brand": "Apple",
                      "category": "smartphones",
                      "thumbnail": "https://example.com/thumb.jpg",
                      "images": ["https://example.com/1.jpg"]
                    }
                  ],
                  "total": 194,
                  "skip": 0,
                  "limit": 10
                }
                """;
    }

    @Test
    void shouldGetAllProductsSuccessfully() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(mockDummyJsonResponse);
        when(favoriteRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(false);

        // When
        ProductListResponse response = productService.getAllProducts(10, 0, 1L);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getProducts().size());
        assertEquals(194, response.getTotal());
        assertEquals(0, response.getSkip());
        assertEquals(10, response.getLimit());

        ProductResponse product = response.getProducts().get(0);
        assertEquals(1L, product.getId());
        assertEquals("iPhone 9", product.getTitle());
        assertEquals(549.0, product.getPrice());
        assertFalse(product.getIsFavorite());

        verify(restTemplate).getForObject(contains("/products?limit=10&skip=0"), eq(String.class));
        verify(favoriteRepository).existsByUserIdAndProductId(1L, 1L);
    }

    @Test
    void shouldMarkProductAsFavoriteWhenUserHasFavorited() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(mockDummyJsonResponse);
        when(favoriteRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(true);

        // When
        ProductListResponse response = productService.getAllProducts(10, 0, 1L);

        // Then
        ProductResponse product = response.getProducts().get(0);
        assertTrue(product.getIsFavorite());
        verify(favoriteRepository).existsByUserIdAndProductId(1L, 1L);
    }

    @Test
    void shouldGetSingleProductById() {
        // Given
        String singleProductResponse = """
                {
                  "id": 5,
                  "title": "Huawei P30",
                  "description": "Huawei phone",
                  "price": 499,
                  "discountPercentage": 10.58,
                  "rating": 4.09,
                  "stock": 32,
                  "brand": "Huawei",
                  "category": "smartphones",
                  "thumbnail": "https://example.com/thumb.jpg",
                  "images": ["https://example.com/1.jpg"]
                }
                """;

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(singleProductResponse);
        when(favoriteRepository.existsByUserIdAndProductId(1L, 5L)).thenReturn(false);

        // When
        ProductResponse product = productService.getProductById(5L, 1L);

        // Then
        assertNotNull(product);
        assertEquals(5L, product.getId());
        assertEquals("Huawei P30", product.getTitle());
        assertEquals(499.0, product.getPrice());
        assertFalse(product.getIsFavorite());

        verify(restTemplate).getForObject(contains("/products/5"), eq(String.class));
        verify(favoriteRepository).existsByUserIdAndProductId(1L, 5L);
    }

    @Test
    void shouldSearchProducts() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(mockDummyJsonResponse);
        when(favoriteRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(false);

        // When
        ProductListResponse response = productService.searchProducts("phone", 1L);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getProducts().size());
        verify(restTemplate).getForObject(contains("/products/search?q=phone"), eq(String.class));
    }

    @Test
    void shouldHandleNullUserIdGracefully() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(mockDummyJsonResponse);

        // When
        ProductListResponse response = productService.getAllProducts(10, 0, null);

        // Then
        assertNotNull(response);
        ProductResponse product = response.getProducts().get(0);
        assertFalse(product.getIsFavorite());

        verify(favoriteRepository, never()).existsByUserIdAndProductId(any(), any());
    }

    @Test
    void shouldThrowExceptionWhenDummyJsonFails() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("API Error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.getAllProducts(10, 0, 1L);
        });

        assertTrue(exception.getMessage().contains("Failed to fetch products"));
    }
}