package com.ecommerce.service;

import com.ecommerce.model.*;
import com.ecommerce.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder().id(1L).username("testuser").build();
        product = Product.builder().id(1L).name("Test Product").price(new BigDecimal("10.00")).build();
        
        cart = Cart.builder().id(1L).user(user).items(new HashSet<>()).build();
        CartItem item = CartItem.builder().product(product).quantity(2).build();
        cart.getItems().add(item);
    }

    @Test
    void testCreateOrder_Success() {
        when(cartService.getCartByUser(user)).thenReturn(cart);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order order = orderService.createOrder(user, "123 Main St");

        assertNotNull(order);
        assertEquals(new BigDecimal("20.00"), order.getTotalPrice());
        assertEquals("PENDING", order.getPaymentStatus());
        assertEquals(1, order.getItems().size());
        verify(cartService, times(1)).clearCart(user);
    }

    @Test
    void testCreateOrder_EmptyCart() {
        cart.getItems().clear();
        when(cartService.getCartByUser(user)).thenReturn(cart);

        assertThrows(RuntimeException.class, () -> orderService.createOrder(user, "123 Main St"));
    }
}
