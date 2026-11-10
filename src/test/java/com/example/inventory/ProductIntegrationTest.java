package com.example.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.inventory.dto.ProductRequest;
import com.example.inventory.dto.ProductResponse;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.service.ProductService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test: starts the real Spring context (service + repository + JPA + auditing)
 * against an in-memory H2 database, so CI needs no PostgreSQL to run it.
 * Each test is rolled back automatically (@Transactional).
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:inventory-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class ProductIntegrationTest {

    @Autowired
    private ProductService service;

    private ProductRequest req(String name, int qty, String price) {
        return new ProductRequest(name, null, qty, new BigDecimal(price));
    }

    @Test
    void create_assignsIdAndTimestamps() {
        ProductResponse created = service.create(req("Gaming Laptop", 5, "1299.99"));

        assertThat(created.id()).isNotNull();
        assertThat(created.createdAt()).isNotNull();
        assertThat(created.updatedAt()).isNotNull();
        assertThat(service.findById(created.id()).name()).isEqualTo("Gaming Laptop");
    }

    @Test
    void search_isCaseInsensitiveAndPartial() {
        service.create(req("Gaming Laptop", 5, "1299.99"));
        service.create(req("Wireless Mouse", 50, "19.90"));

        List<ProductResponse> result = service.findAll("LAPTOP");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Gaming Laptop");
        assertThat(service.findAll(null)).hasSize(2);
    }

    @Test
    void update_changesQuantityAndPrice() {
        Long id = service.create(req("Mouse", 10, "10.00")).id();

        ProductResponse updated = service.update(id, req("Mouse", 25, "12.50"));

        assertThat(updated.quantity()).isEqualTo(25);
        assertThat(updated.price()).isEqualByComparingTo("12.50");
    }

    @Test
    void delete_removesProduct() {
        Long id = service.create(req("Cable", 100, "2.00")).id();

        service.delete(id);

        assertThatThrownBy(() -> service.findById(id)).isInstanceOf(ResourceNotFoundException.class);
    }
}
