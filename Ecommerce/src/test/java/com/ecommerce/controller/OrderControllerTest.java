package com.ecommerce.controller;

import com.ecommerce.dto.CheckoutRequest;
import com.ecommerce.dto.PaymentStatusRequest;
import com.ecommerce.model.Order;
import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OrderController orderController;

    private User user1;
    private User user2;
    private Order order1;
    private Order order2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user1 = User.builder().id(1L).username("user1").build();
        user2 = User.builder().id(2L).username("user2").build();

        order1 = Order.builder().id(1L).user(user1).build();
        order2 = Order.builder().id(2L).user(user2).build();
    }

    @Test
    void testCreateOrder_Success() {
        CheckoutRequest request = new CheckoutRequest();
        request.setShippingAddress("123 Main St");

        when(authentication.getName()).thenReturn("user1");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user1));
        when(orderService.createOrder(user1, "123 Main St")).thenReturn(order1);

        var response = orderController.createOrder(authentication, request);

        assertNotNull(response);
        assertEquals(order1, response.getBody());
        verify(orderService, times(1)).createOrder(user1, "123 Main St");
    }

    @Test
    void testGetOrders_OwnOrders_Success() {
        List<Order> userOrders = Arrays.asList(order1);

        when(authentication.getName()).thenReturn("user1");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user1));
        when(orderService.getOrdersByUser(user1)).thenReturn(userOrders);

        var response = orderController.getOrders(authentication);

        assertNotNull(response);
        assertEquals(1, response.getBody().size());
        verify(orderService, times(1)).getOrdersByUser(user1);
    }

    @Test
    void testGetOrderById_OwnOrder_Success() {
        when(authentication.getName()).thenReturn("user1");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user1));
        when(orderService.getOrderById(1L)).thenReturn(order1);

        var response = orderController.getOrderById(authentication, 1L);

        assertNotNull(response);
        assertEquals(order1, response.getBody());
        verify(orderService, times(1)).getOrderById(1L);
    }

    @Test
    void testGetOrderById_AnotherUsersOrder_AccessDenied() {
        when(authentication.getName()).thenReturn("user1");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user1));
        when(orderService.getOrderById(2L)).thenReturn(order2);

        assertThrows(AccessDeniedException.class, () -> orderController.getOrderById(authentication, 2L));
    }

    @Test
    void testUpdatePaymentStatus_OwnOrder_Success() {
        PaymentStatusRequest request = new PaymentStatusRequest();
        request.setStatus("COMPLETED");

        when(authentication.getName()).thenReturn("user1");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user1));
        when(orderService.getOrderById(1L)).thenReturn(order1);

        var response = orderController.updatePaymentStatus(authentication, 1L, request);

        assertNotNull(response);
        verify(orderService, times(1)).updatePaymentStatus(1L, "COMPLETED");
    }

    @Test
    void testUpdatePaymentStatus_AnotherUsersOrder_AccessDenied() {
        PaymentStatusRequest request = new PaymentStatusRequest();
        request.setStatus("COMPLETED");

        when(authentication.getName()).thenReturn("user1");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user1));
        when(orderService.getOrderById(2L)).thenReturn(order2);

        assertThrows(AccessDeniedException.class, () -> 
            orderController.updatePaymentStatus(authentication, 2L, request));
    }

    @Test
    void testGetOrderById_UserNotFound() {
        when(authentication.getName()).thenReturn("unknownuser");
        when(userRepository.findByUsername("unknownuser")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderController.getOrderById(authentication, 1L));
    }
}
