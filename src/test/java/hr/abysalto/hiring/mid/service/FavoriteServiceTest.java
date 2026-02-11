package hr.abysalto.hiring.mid.service;

import hr.abysalto.hiring.mid.domain.Favorite;
import hr.abysalto.hiring.mid.repository.FavoriteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    private Favorite favorite;

    @BeforeEach
    void setUp() {
        favorite = Favorite.builder()
                .id(1L)
                .userId(1L)
                .productId(5L)
                .addedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldAddProductToFavorites() {
        // Given
        when(favoriteRepository.existsByUserIdAndProductId(1L, 5L)).thenReturn(false);
        when(favoriteRepository.save(any(Favorite.class))).thenReturn(favorite);

        // When
        favoriteService.addToFavorites(1L, 5L);

        // Then
        verify(favoriteRepository).existsByUserIdAndProductId(1L, 5L);
        verify(favoriteRepository).save(any(Favorite.class));
    }

    @Test
    void shouldThrowExceptionWhenProductAlreadyInFavorites() {
        // Given
        when(favoriteRepository.existsByUserIdAndProductId(1L, 5L)).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            favoriteService.addToFavorites(1L, 5L);
        });

        assertEquals("Product already in favorites", exception.getMessage());
        verify(favoriteRepository).existsByUserIdAndProductId(1L, 5L);
        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    @Test
    void shouldRemoveProductFromFavorites() {
        // Given
        when(favoriteRepository.existsByUserIdAndProductId(1L, 5L)).thenReturn(true);
        doNothing().when(favoriteRepository).deleteByUserIdAndProductId(1L, 5L);

        // When
        favoriteService.removeFromFavorites(1L, 5L);

        // Then
        verify(favoriteRepository).existsByUserIdAndProductId(1L, 5L);
        verify(favoriteRepository).deleteByUserIdAndProductId(1L, 5L);
    }

    @Test
    void shouldThrowExceptionWhenRemovingNonExistentFavorite() {
        // Given
        when(favoriteRepository.existsByUserIdAndProductId(1L, 5L)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            favoriteService.removeFromFavorites(1L, 5L);
        });

        assertEquals("Product not in favorites", exception.getMessage());
        verify(favoriteRepository).existsByUserIdAndProductId(1L, 5L);
        verify(favoriteRepository, never()).deleteByUserIdAndProductId(any(), any());
    }

    @Test
    void shouldGetUserFavoriteProductIds() {
        // Given
        Favorite favorite1 = Favorite.builder().userId(1L).productId(5L).build();
        Favorite favorite2 = Favorite.builder().userId(1L).productId(12L).build();
        Favorite favorite3 = Favorite.builder().userId(1L).productId(8L).build();

        when(favoriteRepository.findByUserId(1L))
                .thenReturn(Arrays.asList(favorite1, favorite2, favorite3));

        // When
        List<Long> productIds = favoriteService.getUserFavoriteProductIds(1L);

        // Then
        assertNotNull(productIds);
        assertEquals(3, productIds.size());
        assertTrue(productIds.contains(5L));
        assertTrue(productIds.contains(12L));
        assertTrue(productIds.contains(8L));

        verify(favoriteRepository).findByUserId(1L);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoFavorites() {
        // Given
        when(favoriteRepository.findByUserId(1L)).thenReturn(Arrays.asList());

        // When
        List<Long> productIds = favoriteService.getUserFavoriteProductIds(1L);

        // Then
        assertNotNull(productIds);
        assertTrue(productIds.isEmpty());
        verify(favoriteRepository).findByUserId(1L);
    }
}