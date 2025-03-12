package com.example.Shop.repository;

import com.example.Shop.model.OrderDemo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDemoRepository extends JpaRepository<OrderDemo, Long> {
}