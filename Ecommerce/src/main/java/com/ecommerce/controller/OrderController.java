package com.ecommerce.controller;

import com.ecommerce.dto.CheckoutRequest;
import com.ecommerce.dto.PaymentStatusRequest;
import com.ecommerce.model.Order;
import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/checkout")
    public ResponseEntity<Order> createOrder(Authentication authentication, @RequestBody CheckoutRequest checkoutRequest) {
        User user = getUser(authentication);
        return ResponseEntity.ok(orderService.createOrder(user, checkoutRequest.getShippingAddress()));
    }

    @GetMapping
    public ResponseEntity<List<Order>> getOrders(Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(orderService.getOrdersByUser(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(Authentication authentication, @PathVariable Long id) {
        User user = getUser(authentication);
        Order order = orderService.getOrderById(id);
        
        // Security check: User can only see their own orders
        validateOrderOwnership(user, order);
        
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/payment-status")
    public ResponseEntity<?> updatePaymentStatus(Authentication authentication, @PathVariable Long id, @RequestBody PaymentStatusRequest request) {
        User user = getUser(authentication);
        Order order = orderService.getOrderById(id);
        
        // Security check: User can only update their own orders
        validateOrderOwnership(user, order);
        
        orderService.updatePaymentStatus(id, request.getStatus());
        return ResponseEntity.ok().build();
    }

    private User getUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void validateOrderOwnership(User user, Order order) {
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to access this order");
        }
    }
}

