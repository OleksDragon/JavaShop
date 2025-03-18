package com.example.Shop.Service;

import com.example.Shop.model.OrderDemo;
import com.example.Shop.model.OrderItem;
import com.example.Shop.model.Product;
import com.example.Shop.model.User;
import com.example.Shop.repository.OrderDemoRepository;
import com.example.Shop.repository.OrderItemRepository;
import com.example.Shop.repository.ProductRepository;
import com.example.Shop.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final OrderDemoRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;
    private final UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    public OrderService(OrderDemoRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        ProductService productService,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productService = productService;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderDemo createOrder(OrderDemo order, Map<Long, Integer> productQuantities) {
        // Отримуємо ім’я користувача з SecurityContext
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // Завантажуємо користувача з бази даних
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Користувача з іменем " + username + " не знайдено"));

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();

            if (quantity == null || quantity <= 0) {
                continue;
            }

            Product product = productService.getProductById(productId);
            if (product.getStock() < quantity) {
                throw new IllegalArgumentException("Недостатньо товару " + product.getName() + " на складі");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setPrice(product.getPrice());
            orderItem.setOrder(order);
            orderItem = orderItemRepository.save(orderItem);

            product.setStock(product.getStock() - quantity);
            productService.updateProduct(product);

            orderItems.add(orderItem);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        }

        if (orderItems.isEmpty()) {
            throw new IllegalArgumentException("Замовлення не може бути порожнім");
        }

        order.setUser(currentUser);
        order.setOrdersItems(orderItems);
        order.setTotal(total);

        OrderDemo savedOrder = orderRepository.save(order);

        return savedOrder;
    }

    @Transactional(readOnly = true)
    public List<OrderDemo> getAllOrders() {
        List<OrderDemo> orders = orderRepository.findAll();
        orders.forEach(order -> {
            order.getOrdersItems().size();
            order.getOrdersItems().forEach(item -> item.getProduct().getName());
            System.out.println("Order ID: " + order.getId() + ", Items count: " + order.getOrdersItems().size());
        });
        return orders;
    }

    public OrderDemo getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Transactional
    public void updateOrder(Long orderId, Map<Long, Integer> quantities) {
        OrderDemo order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        // Відновлюємо запаси для старих товарів перед зміною
        for (OrderItem item : new ArrayList<>(order.getOrdersItems())) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
            orderItemRepository.delete(item); // Видаляємо старі OrderItem із бази
        }
        order.getOrdersItems().clear(); // Очищаємо список у пам’яті

        // Додаємо оновлені товари та перераховуємо total
        List<OrderItem> updatedItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            Long productId = entry.getKey();
            Integer newQuantity = entry.getValue();

            if (newQuantity == null || newQuantity < 0) {
                continue; // Пропускаємо товари з некоректною кількістю
            }

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + productId));
            int availableStock = product.getStock();

            if (newQuantity > availableStock) {
                throw new IllegalArgumentException("Not enough stock for product ID: " + productId +
                        ", required: " + newQuantity + ", available: " + availableStock);
            }

            if (newQuantity > 0) {
                OrderItem item = order.getOrdersItems().stream()
                        .filter(i -> i.getProduct().getId() == productId)
                        .findFirst()
                        .orElse(new OrderItem()); // Якщо товар уже є, беремо його, інакше створюємо новий

                item.setOrder(order);
                item.setProduct(product);
                item.setQuantity(newQuantity);
                item.setPrice(product.getPrice());

                updatedItems.add(item);
                total = total.add(product.getPrice().multiply(BigDecimal.valueOf(newQuantity)));
                product.setStock(availableStock - newQuantity); // Оновлюємо запаси
                productRepository.save(product);
            }
        }

        // Оновлюємо список товарів і загальну суму
        order.getOrdersItems().clear();
        order.getOrdersItems().addAll(updatedItems);
        order.setTotal(total);
        orderRepository.save(order);
    }

    public void deleteOrder(Long id) {
        OrderDemo order = orderRepository.findById(id).orElse(null);
        if (order != null) {
            for (OrderItem item : order.getOrdersItems()) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
            orderRepository.delete(order);
        }
    }
}