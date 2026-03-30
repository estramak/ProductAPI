package com.example.productapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

//marks the class as a JPA entity
@Entity
public class Product {
    //Specifies the primary key
    @Id
    //Automatically generates the ID value
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private double price;

    public Product() {}

    //Getters
    public String getName() {
        return name;
    }
    public double getPrice() {
        return price;
    }
    public Long getId() { return id; }

    //Setters
    public void setName(String name) {
        this.name = name;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public void setId(Long id) { this.id = id; }
}
