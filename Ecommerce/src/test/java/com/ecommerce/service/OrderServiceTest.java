package com.ecommerce.service;

import com.ecommerce.model.*;
import com.ecommerce.repository.CartItemRepository;
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

    @Mock
    private CartItemRepository cartItemRepository;

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

    @Test
    void testCreateOrder_NullCartItems() {
        cart.setItems(null);
        when(cartService.getCartByUser(user)).thenReturn(cart);

        assertThrows(RuntimeException.class, () -> orderService.createOrder(user, "123 Main St"));
    }

    @Test
    void testCreateOrder_MissingShippingAddress() {
        when(cartService.getCartByUser(user)).thenReturn(cart);

        assertThrows(RuntimeException.class, () -> orderService.createOrder(user, null));
        assertThrows(RuntimeException.class, () -> orderService.createOrder(user, "   "));
    }

    @Test
    void testCreateOrder_MultipleItems() {
        Product product2 = Product.builder().id(2L).name("Test Product 2").price(new BigDecimal("15.00")).build();
        CartItem item2 = CartItem.builder().product(product2).quantity(3).build();
        cart.getItems().add(item2);

        when(cartService.getCartByUser(user)).thenReturn(cart);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order order = orderService.createOrder(user, "123 Main St");

        assertNotNull(order);
        assertEquals(new BigDecimal("65.00"), order.getTotalPrice()); // (10*2) + (15*3) = 65
        assertEquals(2, order.getItems().size());
    }

    @Test
    void testGetOrderById_Success() {
        Order order = Order.builder().id(1L).user(user).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order retrievedOrder = orderService.getOrderById(1L);

        assertNotNull(retrievedOrder);
        assertEquals(1L, retrievedOrder.getId());
    }

    @Test
    void testGetOrderById_NotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.getOrderById(999L));
    }

    @Test
    void testUpdatePaymentStatus_Success() {
        Order order = Order.builder().id(1L).user(user).paymentStatus("PENDING").build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.updatePaymentStatus(1L, "COMPLETED");

        assertEquals("COMPLETED", order.getPaymentStatus());
        verify(orderRepository, times(1)).save(order);
    }
}
