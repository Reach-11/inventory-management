package com.example.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.inventory.dto.ProductRequest;
import com.example.inventory.dto.ProductResponse;
import com.example.inventory.entity.Product;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Pure unit test: no Spring, no database. The repository is a Mockito fake. */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService service;

    private ProductRequest request(String name, int qty, String price) {
        return new ProductRequest(name, "desc", qty, new BigDecimal(price));
    }

    @Test
    void create_savesProductAndReturnsResponse() {
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse response = service.create(request("  Keyboard ", 10, "49.90"));

        assertThat(response.name()).isEqualTo("Keyboard");   // trimmed
        assertThat(response.quantity()).isEqualTo(10);
        assertThat(response.price()).isEqualByComparingTo("49.90");
    }

    @Test
    void findById_whenMissing_throwsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void update_changesFieldsOfExistingProduct() {
        Product existing = new Product();
        existing.setName("Old");
        existing.setQuantity(1);
        existing.setPrice(new BigDecimal("1.00"));
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.saveAndFlush(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse response = service.update(1L, request("New", 7, "2.50"));

        assertThat(response.name()).isEqualTo("New");
        assertThat(response.quantity()).isEqualTo(7);
        assertThat(response.price()).isEqualByComparingTo("2.50");
    }

    @Test
    void delete_whenMissing_throwsNotFoundAndDeletesNothing() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }

    @Test
    void findAll_withName_usesSearchQuery() {
        Product p = new Product();
        p.setName("Laptop");
        p.setPrice(BigDecimal.TEN);
        when(repository.findByNameContainingIgnoreCaseOrderByIdAsc("lap")).thenReturn(List.of(p));

        List<ProductResponse> result = service.findAll(" lap ");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Laptop");
    }
}
