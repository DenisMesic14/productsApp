package hr.abysalto.hiring.mid.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.abysalto.hiring.mid.domain.Product;
import hr.abysalto.hiring.mid.dto.response.ProductListResponse;
import hr.abysalto.hiring.mid.dto.response.ProductResponse;
import hr.abysalto.hiring.mid.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    @Value("${dummyjson.base-url}")
    private String dummyJsonBaseUrl;

    private final FavoriteRepository favoriteRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Cacheable(value = "products", key = "#limit + '_' + #skip")
    public ProductListResponse getAllProducts(Integer limit, Integer skip, Long userId) {
        String url = String.format("%s/products?limit=%d&skip=%d", dummyJsonBaseUrl, limit, skip);

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            List<ProductResponse> products = new ArrayList<>();
            JsonNode productsArray = root.get("products");

            for (JsonNode productNode : productsArray) {
                Product product = objectMapper.treeToValue(productNode, Product.class);
                ProductResponse productResponse = mapToResponse(product, userId);
                products.add(productResponse);
            }

            return ProductListResponse.builder()
                    .products(products)
                    .total(root.get("total").asInt())
                    .skip(root.get("skip").asInt())
                    .limit(root.get("limit").asInt())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch products from DummyJSON API", e);
        }
    }

    @Cacheable(value = "product", key = "#productId")
    public ProductResponse getProductById(Long productId, Long userId) {
        String url = String.format("%s/products/%d", dummyJsonBaseUrl, productId);

        try {
            String response = restTemplate.getForObject(url, String.class);
            Product product = objectMapper.readValue(response, Product.class);
            return mapToResponse(product, userId);

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch product from DummyJSON API", e);
        }
    }

    public ProductListResponse searchProducts(String query, Long userId) {
        String url = String.format("%s/products/search?q=%s", dummyJsonBaseUrl, query);

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            List<ProductResponse> products = new ArrayList<>();
            JsonNode productsArray = root.get("products");

            for (JsonNode productNode : productsArray) {
                Product product = objectMapper.treeToValue(productNode, Product.class);
                ProductResponse productResponse = mapToResponse(product, userId);
                products.add(productResponse);
            }

            return ProductListResponse.builder()
                    .products(products)
                    .total(root.get("total").asInt())
                    .skip(0)
                    .limit(products.size())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to search products from DummyJSON API", e);
        }
    }

    private ProductResponse mapToResponse(Product product, Long userId) {
        boolean isFavorite = false;
        if (userId != null) {
            isFavorite = favoriteRepository.existsByUserIdAndProductId(userId, product.getId());
        }

        return ProductResponse.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .discountPercentage(product.getDiscountPercentage())
                .rating(product.getRating())
                .stock(product.getStock())
                .brand(product.getBrand())
                .category(product.getCategory())
                .thumbnail(product.getThumbnail())
                .images(product.getImages())
                .isFavorite(isFavorite)
                .build();
    }
}