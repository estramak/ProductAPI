package com.example.productapi.repository;

import com.example.productapi.model.Product;
//provides basic CRUD operations and allows for additional custom queries
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long>{

}
