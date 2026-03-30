package com.ecommerce.service;

import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder().id(1L).username("testuser").build();
        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(new BigDecimal("10.00"))
                .stockQuantity(100)
                .build();
        cart = Cart.builder().id(1L).user(user).items(new HashSet<>()).build();
    }

    @Test
    void testAddItemToCart_NewItem() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart updatedCart = cartService.addItemToCart(user, 1L, 2);

        assertEquals(1, updatedCart.getItems().size());
        CartItem item = updatedCart.getItems().iterator().next();
        assertEquals(2, item.getQuantity());
        assertEquals(product, item.getProduct());
    }

    @Test
    void testAddItemToCart_ExistingItem() {
        CartItem existingItem = CartItem.builder().cart(cart).product(product).quantity(1).build();
        cart.getItems().add(existingItem);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart updatedCart = cartService.addItemToCart(user, 1L, 2);

        assertEquals(1, updatedCart.getItems().size());
        assertEquals(3, existingItem.getQuantity());
    }

    @Test
    void testAddItemToCart_InvalidQuantity() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        assertThrows(RuntimeException.class, () -> cartService.addItemToCart(user, 1L, 0));
        assertThrows(RuntimeException.class, () -> cartService.addItemToCart(user, 1L, -5));
    }

    @Test
    void testAddItemToCart_InsufficientStock() {
        product.setStockQuantity(5);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(RuntimeException.class, () -> cartService.addItemToCart(user, 1L, 10));
    }

    @Test
    void testAddItemToCart_NullCartItems() {
        cart.setItems(null); // Simulate null items
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // Should handle null items gracefully
        assertDoesNotThrow(() -> cartService.addItemToCart(user, 1L, 2));
        assertNotNull(cart.getItems());
    }

    @Test
    void testUpdateItemQuantity_ValidUpdate() {
        CartItem item = CartItem.builder().cart(cart).product(product).quantity(2).build();
        cart.getItems().add(item);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart updatedCart = cartService.updateItemQuantity(user, 1L, 5);

        assertEquals(5, item.getQuantity());
    }

    @Test
    void testUpdateItemQuantity_RemoveItem() {
        CartItem item = CartItem.builder().cart(cart).product(product).quantity(2).build();
        cart.getItems().add(item);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart updatedCart = cartService.updateItemQuantity(user, 1L, 0);

        assertEquals(0, updatedCart.getItems().size());
    }

    @Test
    void testUpdateItemQuantity_InvalidQuantity() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        assertThrows(RuntimeException.class, () -> cartService.updateItemQuantity(user, 1L, -1));
    }

    @Test
    void testRemoveItemFromCart_Success() {
        CartItem item = CartItem.builder().cart(cart).product(product).quantity(1).build();
        cart.getItems().add(item);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart updatedCart = cartService.removeItemFromCart(user, 1L);

        assertEquals(0, updatedCart.getItems().size());
    }

    @Test
    void testRemoveItemFromCart_ItemNotFound() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        assertThrows(RuntimeException.class, () -> cartService.removeItemFromCart(user, 1L));
    }

    @Test
    void testClearCart_Success() {
        CartItem item = CartItem.builder().cart(cart).product(product).quantity(1).build();
        cart.getItems().add(item);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.clearCart(user);

        assertEquals(0, cart.getItems().size());
    }
}
