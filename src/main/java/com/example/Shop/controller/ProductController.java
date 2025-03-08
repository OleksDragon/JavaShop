package com.example.Shop.controller;

import com.example.Shop.Service.ProductService;
import com.example.Shop.model.Product;
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

    @GetMapping("/save")
    public String showSaveForm(Model model) {
        model.addAttribute("product", new Product());
        return "crud/save";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Product product) {
        productService.addProduct(product);
        return "redirect:/products";
    }

    // Отображение формы редактирования
    @GetMapping("/update/{id}")
    public String editProduct(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "crud/update";
    }

    // Обновление товара
    @PostMapping("/update/{id}")
    public String updateProduct(@ModelAttribute Product product) {
        productService.updateProduct(product);
        return "redirect:/products";
    }

    // Удаление товара
    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/products";
    }
}