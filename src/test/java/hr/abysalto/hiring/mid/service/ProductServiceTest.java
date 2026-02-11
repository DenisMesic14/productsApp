package hr.abysalto.hiring.mid.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.abysalto.hiring.mid.domain.Favorite;
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

import java.util.Collections;
import java.util.List;

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
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(mockDummyJsonResponse);
        when(favoriteRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        ProductListResponse response = productService.getAllProducts(10, 0, null, "asc", 1L);

        assertNotNull(response);
        assertEquals(1, response.getProducts().size());
        assertFalse(response.getProducts().get(0).getIsFavorite());
        verify(restTemplate).getForObject(contains("/products?limit=10&skip=0"), eq(String.class));
        verify(favoriteRepository, times(1)).findByUserId(1L);
    }

    @Test
    void shouldGetAllProductsWithSorting() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(mockDummyJsonResponse);
        when(favoriteRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        productService.getAllProducts(10, 0, "price", "desc", 1L);

        verify(restTemplate).getForObject(argThat((String url) ->
                url != null && url.contains("sortBy=price") && url.contains("order=desc")
        ), eq(String.class));
    }

    @Test
    void shouldMarkProductAsFavoriteWhenUserHasFavorited() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(mockDummyJsonResponse);

        Favorite favorite = new Favorite();
        favorite.setUserId(1L);
        favorite.setProductId(1L);
        when(favoriteRepository.findByUserId(1L)).thenReturn(List.of(favorite));

        ProductListResponse response = productService.getAllProducts(10, 0, null, "asc", 1L);

        assertTrue(response.getProducts().get(0).getIsFavorite());
        verify(favoriteRepository, times(1)).findByUserId(1L);
    }

    @Test
    void shouldGetSingleProductById() {
        String singleProductResponse = """
                {
                  "id": 5,
                  "title": "Huawei P30",
                  "price": 499,
                  "description": "Huawei phone",
                  "discountPercentage": 10.5,
                  "rating": 4.5,
                  "stock": 50,
                  "brand": "Huawei",
                  "category": "smartphones",
                  "thumbnail": "https://example.com/thumb.jpg",
                  "images": ["https://example.com/1.jpg"]
                }
                """;
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(singleProductResponse);
        when(favoriteRepository.existsByUserIdAndProductId(1L, 5L)).thenReturn(false);

        ProductResponse product = productService.getProductById(5L, 1L);

        assertNotNull(product);
        assertEquals(5L, product.getId());
        assertEquals("Huawei P30", product.getTitle());
        assertFalse(product.getIsFavorite());
        verify(restTemplate).getForObject(contains("/products/5"), eq(String.class));
    }

    @Test
    void shouldSearchProducts() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockDummyJsonResponse);
        when(favoriteRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        ProductListResponse response = productService.searchProducts("phone", 1L);

        assertNotNull(response);
        assertEquals(1, response.getProducts().size());
        verify(restTemplate).getForObject(contains("/products/search?q=phone"), eq(String.class));
        verify(favoriteRepository, times(1)).findByUserId(1L);
    }

    @Test
    void shouldHandleNullUserIdGracefully() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockDummyJsonResponse);

        ProductListResponse response = productService.getAllProducts(10, 0, null, "asc", null);

        assertNotNull(response);
        verify(favoriteRepository, never()).findByUserId(any());
        verify(favoriteRepository, never()).existsByUserIdAndProductId(any(), any());
    }

    @Test
    void shouldThrowExceptionWhenDummyJsonFails() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("API Error"));

        assertThrows(RuntimeException.class, () -> {
            productService.getAllProducts(10, 0, null, "asc", 1L);
        });
    }

    @Test
    void shouldHandleMultipleFavoritesCorrectly() {
        String multipleProductsResponse = """
                {
                  "products": [
                    {
                      "id": 1,
                      "title": "Product 1",
                      "description": "Description 1",
                      "price": 100,
                      "discountPercentage": 10,
                      "rating": 4.5,
                      "stock": 50,
                      "brand": "Brand1",
                      "category": "category1",
                      "thumbnail": "thumb1.jpg",
                      "images": ["img1.jpg"]
                    },
                    {
                      "id": 2,
                      "title": "Product 2",
                      "description": "Description 2",
                      "price": 200,
                      "discountPercentage": 15,
                      "rating": 4.8,
                      "stock": 30,
                      "brand": "Brand2",
                      "category": "category2",
                      "thumbnail": "thumb2.jpg",
                      "images": ["img2.jpg"]
                    }
                  ],
                  "total": 2,
                  "skip": 0,
                  "limit": 10
                }
                """;

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(multipleProductsResponse);

        Favorite favorite = new Favorite();
        favorite.setUserId(1L);
        favorite.setProductId(2L);
        when(favoriteRepository.findByUserId(1L)).thenReturn(List.of(favorite));

        ProductListResponse response = productService.getAllProducts(10, 0, null, "asc", 1L);

        assertEquals(2, response.getProducts().size());
        assertFalse(response.getProducts().get(0).getIsFavorite());
        assertTrue(response.getProducts().get(1).getIsFavorite());
        verify(favoriteRepository, times(1)).findByUserId(1L);
    }
}