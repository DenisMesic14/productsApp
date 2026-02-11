package hr.abysalto.hiring.mid.controller;

import hr.abysalto.hiring.mid.domain.User;
import hr.abysalto.hiring.mid.dto.response.ProductListResponse;
import hr.abysalto.hiring.mid.dto.response.ProductResponse;
import hr.abysalto.hiring.mid.service.ProductService;
import hr.abysalto.hiring.mid.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private UserService userService;

    private User testUser;
    private ProductResponse product1;
    private ProductResponse product2;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("$2a$10$hashedPassword")
                .email("test@example.com")
                .enabled(true)
                .build();

        product1 = ProductResponse.builder()
                .id(1L)
                .title("iPhone 9")
                .description("An apple mobile")
                .price(549.0)
                .rating(4.69)
                .stock(94)
                .brand("Apple")
                .category("smartphones")
                .isFavorite(false)
                .build();

        product2 = ProductResponse.builder()
                .id(2L)
                .title("iPhone X")
                .description("An apple mobile")
                .price(899.0)
                .rating(4.44)
                .stock(34)
                .brand("Apple")
                .category("smartphones")
                .isFavorite(true)
                .build();
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldGetAllProducts() throws Exception {
        // Given
        ProductListResponse response = ProductListResponse.builder()
                .products(Arrays.asList(product1, product2))
                .total(194)
                .skip(0)
                .limit(10)
                .build();

        when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        when(productService.getAllProducts(10, 0, null, "asc", 1L)).thenReturn(response);


        mockMvc.perform(get("/api/products")
                        .param("limit", "10")
                        .param("skip", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products", hasSize(2)))
                .andExpect(jsonPath("$.products[0].id").value(1))
                .andExpect(jsonPath("$.products[0].title").value("iPhone 9"))
                .andExpect(jsonPath("$.total").value(194));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldGetProductById() throws Exception {
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(productService.getProductById(1L, 1L)).thenReturn(product1);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldSearchProducts() throws Exception {
        ProductListResponse response = ProductListResponse.builder()
                .products(Arrays.asList(product1, product2))
                .total(2)
                .skip(0)
                .limit(2)
                .build();

        when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(productService.searchProducts("phone", 1L)).thenReturn(response);

        mockMvc.perform(get("/api/products/search")
                        .param("q", "phone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products", hasSize(2)));
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldUseDefaultPaginationParameters() throws Exception {
        ProductListResponse response = ProductListResponse.builder()
                .products(Collections.singletonList(product1))
                .total(194)
                .skip(0)
                .limit(10)
                .build();

        when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(productService.getAllProducts(10, 0, null, "asc", 1L)).thenReturn(response);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skip").value(0))
                .andExpect(jsonPath("$.limit").value(10));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldHandleCustomSortingParameters() throws Exception {
        // Given
        ProductListResponse response = ProductListResponse.builder()
                .products(Collections.singletonList(product2))
                .total(194)
                .skip(0)
                .limit(10)
                .build();

        when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(productService.getAllProducts(10, 0, "price", "desc", 1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/products")
                        .param("sortBy", "price")
                        .param("order", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products[0].title").value("iPhone X"));
    }
}