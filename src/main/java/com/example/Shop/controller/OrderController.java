package com.example.Shop.controller;

import com.example.Shop.Service.OrderService;
import com.example.Shop.Service.ProductService;
import com.example.Shop.model.OrderDemo;
import com.example.Shop.model.Product;
import jakarta.persistence.EntityNotFoundException;
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable Long id, Model model) {
        OrderDemo order = orderService.getOrderById(id);
        if (order == null) {
            return "redirect:/orders";
        }
        model.addAttribute("order", order);
        model.addAttribute("products", productService.getAllProducts());
        return "orders/update";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/update/{id}")
    public String updateOrder(@PathVariable Long id, @ModelAttribute OrderDemo order,
                              @RequestParam Map<String, String> allParams, Model model) {
        System.out.println("Updating order ID: " + id);
        Map<Long, Integer> productQuantities = new HashMap<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().startsWith("quantities[")) {
                try {
                    String key = entry.getKey().replace("quantities[", "").replace("]", "");
                    Long productId = Long.parseLong(key);
                    Integer quantity = Integer.parseInt(entry.getValue());
                    if (quantity >= 0) { // Дозволяємо 0 для видалення
                        productQuantities.put(productId, quantity);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid quantity format for key: " + entry.getKey());
                }
            }
        }
        try {
            orderService.updateOrder(id, productQuantities);
            return "redirect:/orders";
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("order", orderService.getOrderById(id));
            model.addAttribute("products", productService.getAllProducts());
            return "orders/update";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return "redirect:/orders";
    }
}