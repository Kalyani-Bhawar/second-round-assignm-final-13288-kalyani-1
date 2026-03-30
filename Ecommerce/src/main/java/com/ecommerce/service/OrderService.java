package com.ecommerce.service;

import com.ecommerce.model.*;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Transactional
    public Order createOrder(User user, String shippingAddress) {
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            throw new RuntimeException("Shipping address is required");
        }

        Cart cart = cartService.getCartByUser(user);
        
        // Validate cart items are not null and not empty
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Persist cart items first to ensure they have database IDs
        cartItemRepository.saveAll(cart.getItems());

        // Calculate total price
        BigDecimal total = calculateCartTotal(cart);

        // Create order with items
        Order order = createOrderFromCart(user, shippingAddress, total, cart);
        
        Order savedOrder = orderRepository.save(order);
        cartService.clearCart(user);
        return savedOrder;
    }

    private BigDecimal calculateCartTotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> {
                    if (item.getProduct().getPrice() == null) {
                        throw new RuntimeException("Product price is missing");
                    }
                    return item.getProduct().getPrice()
                            .multiply(new BigDecimal(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Order createOrderFromCart(User user, String shippingAddress, BigDecimal total, Cart cart) {
        Order order = Order.builder()
                .user(user)
                .shippingAddress(shippingAddress)
                .totalPrice(total)
                .paymentStatus("PENDING")
                .orderDate(LocalDateTime.now())
                .build();

        order.setItems(cart.getItems().stream()
                .map(cartItem -> OrderItem.builder()
                        .order(order)
                        .product(cartItem.getProduct())
                        .quantity(cartItem.getQuantity())
                        .price(cartItem.getProduct().getPrice())
                        .build())
                .collect(Collectors.toSet()));

        return order;
    }

    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUser(user);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public void updatePaymentStatus(Long orderId, String status) {
        Order order = getOrderById(orderId);
        order.setPaymentStatus(status);
        orderRepository.save(order);
    }
}
