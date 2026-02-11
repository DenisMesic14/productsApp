package hr.abysalto.hiring.mid.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

    private Long id;
    private Long productId;
    private String productTitle;
    private Double productPrice;
    private Integer quantity;
    private Double subtotal;
    private LocalDateTime addedAt;
}