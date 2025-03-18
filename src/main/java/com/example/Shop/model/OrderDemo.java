package com.example.Shop.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @JsonBackReference
    private User user;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<OrderItem> ordersItems;
    @PositiveOrZero
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal total;

    public void addProduct(Product product, int quantity) {
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(quantity);
        orderItem.setOrder(this);

        this.ordersItems.add(orderItem);

        if (this.total == null) {
            this.total = BigDecimal.ZERO;
        }
        BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        this.total = this.total.add(itemTotal);
    }
}