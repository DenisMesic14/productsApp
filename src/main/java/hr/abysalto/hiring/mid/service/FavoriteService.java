package hr.abysalto.hiring.mid.service;

import hr.abysalto.hiring.mid.domain.Favorite;
import hr.abysalto.hiring.mid.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    public void addToFavorites(Long userId, Long productId) {
        // Check if already exists
        if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new RuntimeException("Product already in favorites");
        }

        Favorite favorite = Favorite.builder()
                .userId(userId)
                .productId(productId)
                .addedAt(LocalDateTime.now())
                .build();

        favoriteRepository.save(favorite);
    }

    public void removeFromFavorites(Long userId, Long productId) {
        if (!favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new RuntimeException("Product not in favorites");
        }

        favoriteRepository.deleteByUserIdAndProductId(userId, productId);
    }

    public List<Long> getUserFavoriteProductIds(Long userId) {
        return favoriteRepository.findByUserId(userId)
                .stream()
                .map(Favorite::getProductId)
                .collect(Collectors.toList());
    }
}