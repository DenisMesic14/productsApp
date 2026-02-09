package hr.abysalto.hiring.mid.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("CART_ITEMS")
public class CartItem {

    @Id
    private Long id;

    private Long userId;

    private Long productId;

    private String productTitle;

    private Double productPrice;

    private Integer quantity;

    private LocalDateTime addedAt;
}
