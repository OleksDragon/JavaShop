package com.example.Shop.controller;

import com.example.Shop.Service.OrderService;
import com.example.Shop.Service.ProductService;
import com.example.Shop.model.OrderDemo;
import com.example.Shop.model.Product;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final ProductService productService;

    public OrderController(OrderService orderService, ProductService productService) {
        this.orderService = orderService;
        this.productService = productService;
    }

    @GetMapping("/create")
    public String showOrderForm(Model model, HttpSession session) {
        @SuppressWarnings("unchecked")
        Map<Long, Product> cart = (Map<Long, Product>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            return "redirect:/products";
        }
        model.addAttribute("order", new OrderDemo());
        model.addAttribute("cart", cart);
        return "orders/create";
    }

    @PostMapping("/create")
    public String createOrder(@ModelAttribute OrderDemo order,
                              @RequestParam Map<String, String> allParams,
                              HttpSession session) {

        @SuppressWarnings("unchecked")
        Map<Long, Product> cart = (Map<Long, Product>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            return "redirect:/products";
        }

        Map<Long, Integer> productQuantities = new HashMap<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().startsWith("quantities[")) {
                try {
                    String key = entry.getKey().replace("quantities[", "").replace("]", "");
                    Long productId = Long.parseLong(key);
                    Integer quantity = Integer.parseInt(entry.getValue());
                    if (cart.containsKey(productId) && quantity > 0) {
                        productQuantities.put(productId, quantity);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid quantity format for key: " + entry.getKey());
                }
            }
        }

        if (!productQuantities.isEmpty()) {
            orderService.createOrder(order, productQuantities);
            session.removeAttribute("cart");
        } else {
            System.out.println("No valid quantities found in request");
        }
        return "redirect:/products";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public String getAllOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "orders/list";
    }

    @PostMapping("/addToCart/{id}")
    public String addToCart(@PathVariable Long id, HttpSession session) {
        @SuppressWarnings("unchecked")
        Map<Long, Product> cart = (Map<Long, Product>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
        }
        Product product = productService.getProductById(id);
        if (product != null && product.getStock() > 0) {
            cart.put(id, product);
        }
        session.setAttribute("cart", cart);
        return "redirect:/products";
    }
}