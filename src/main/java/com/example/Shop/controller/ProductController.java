package com.example.Shop.controller;

import com.example.Shop.model.Product;
import com.example.Shop.Service.ProductService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String read(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "crud/read";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/save")
    public String showSaveForm(Model model) {
        model.addAttribute("product", new Product());
        return "crud/save";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/save")
    public String save(@ModelAttribute Product product) {
        productService.addProduct(product);
        return "redirect:/products";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/update/{id}")
    public String editProduct(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "crud/update";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/update/{id}")
    public String updateProduct(@ModelAttribute Product product) {
        productService.updateProduct(product);
        return "redirect:/products";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/products";
    }

    // Перенаправлення на кошик через OrderController
    @PostMapping("/addToCart/{id}")
    public String addToCartRedirect(@PathVariable Long id) {
        return "redirect:/orders/addToCart/" + id;
    }
}