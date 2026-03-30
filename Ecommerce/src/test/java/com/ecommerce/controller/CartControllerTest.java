package com.ecommerce.controller;

import com.ecommerce.dto.CartRequest;
import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CartControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CartController cartController;

    private User user1;
    private User user2;
    private Cart cart1;
    private Cart cart2;
    private Product product;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user1 = User.builder().id(1L).username("user1").build();
        user2 = User.builder().id(2L).username("user2").build();

        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(new BigDecimal("10.00"))
                .stockQuantity(100)
                .build();

        cart1 = Cart.builder().id(1L).user(user1).items(new HashSet<>()).build();
        cart2 = Cart.builder().id(2L).user(user2).items(new HashSet<>()).build();
    }

    @Test
    void testGetCart_OwnCart_Success() {
        when(authentication.getName()).thenReturn("user1");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user1));
        when(cartService.getCartByUser(user1)).thenReturn(cart1);

        var response = cartController.getCart(authentication);

        assertNotNull(response);
        assertEquals(cart1, response.getBody());
        verify(cartService, times(1)).getCartByUser(user1);
    }

    @Test
    void testGetCart_UserNotFound() {
        when(authentication.getName()).thenReturn("unknownuser");
        when(userRepository.findByUsername("unknownuser")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> cartController.getCart(authentication));
    }

    @Test
    void testAddItemToCart_OwnCart_Success() {
        CartRequest request = new CartRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        when(authentication.getName()).thenReturn("user1");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user1));
        when(cartService.addItemToCart(user1, 1L, 2)).thenReturn(cart1);

        var response = cartController.addItemToCart(authentication, request);

        assertNotNull(response);
        assertEquals(cart1, response.getBody());
        verify(cartService, times(1)).addItemToCart(user1, 1L, 2);
    }

    @Test
    void testRemoveItemFromCart_OwnCart_Success() {
        when(authentication.getName()).thenReturn("user1");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user1));
        when(cartService.removeItemFromCart(user1, 1L)).thenReturn(cart1);

        var response = cartController.removeItemFromCart(authentication, 1L);

        assertNotNull(response);
        verify(cartService, times(1)).removeItemFromCart(user1, 1L);
    }

    @Test
    void testClearCart_OwnCart_Success() {
        when(authentication.getName()).thenReturn("user1");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user1));
        when(cartService.getCartByUser(user1)).thenReturn(cart1);

        var response = cartController.clearCart(authentication);

        assertNotNull(response);
        verify(cartService, times(1)).clearCart(user1);
    }
}
