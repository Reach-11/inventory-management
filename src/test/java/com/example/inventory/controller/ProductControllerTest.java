package com.example.inventory.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.inventory.dto.ProductRequest;
import com.example.inventory.dto.ProductResponse;
import com.example.inventory.exception.GlobalExceptionHandler;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.service.ProductService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Tests the HTTP layer only (URLs, status codes, validation, error JSON).
 * Uses MockMvc in "standalone" mode: no Spring context, so it is fast.
 */
@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService service;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ProductController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private ProductResponse sample(long id) {
        return new ProductResponse(id, "Laptop", "16GB", 5, new BigDecimal("1299.99"), Instant.now(), Instant.now());
    }

    @Test
    void post_validBody_returns201WithLocation() throws Exception {
        when(service.create(any(ProductRequest.class))).thenReturn(sample(1L));

        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Laptop","description":"16GB","quantity":5,"price":1299.99}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/products/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    void post_invalidBody_returns400WithFieldErrors() throws Exception {
        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","quantity":-1,"price":10}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.quantity").exists());
    }

    @Test
    void post_malformedJson_returns400() throws Exception {
        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void get_unknownId_returns404() throws Exception {
        when(service.findById(99L)).thenThrow(new ResourceNotFoundException("Product with id 99 not found"));

        mvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product with id 99 not found"));
    }

    @Test
    void get_nonNumericId_returns400() throws Exception {
        mvc.perform(get("/api/products/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void get_all_returnsList() throws Exception {
        when(service.findAll(null)).thenReturn(List.of(sample(1L), sample(2L)));

        mvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void delete_returns204() throws Exception {
        mvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
        verify(service).delete(1L);
    }
}
