package com.example.Shop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Setter
@Getter
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Size(min = 2, max = 255, message = "Name min: 2, max: 255")
    @Column(nullable = false, length = 255)
    private String name;
    @Size(min = 2, max = 1024, message = "Description min: 2, max: 1024")
    @Column(nullable = true, length = 1024)
    private String description;
    @Positive
    @Column(precision = 8, scale = 2, nullable = false)
    private BigDecimal price;
    @PositiveOrZero
    @Column(nullable = false, length = Integer.MAX_VALUE)
    private int stock;
}
