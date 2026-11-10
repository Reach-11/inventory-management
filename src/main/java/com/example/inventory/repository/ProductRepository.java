package com.example.inventory.repository;

import com.example.inventory.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /** SQL roughly: WHERE lower(name) LIKE lower('%text%') ORDER BY id. Spring builds it from the method name. */
    List<Product> findByNameContainingIgnoreCaseOrderByIdAsc(String name);
}
