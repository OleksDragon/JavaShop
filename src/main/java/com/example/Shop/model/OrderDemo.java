package com.example.Shop.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
@Setter
@Getter
@Entity
public class OrderDemo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    private User user;
    @OneToMany
    private List<OrderItem> ordersItems;
    @PositiveOrZero
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal total;
}
