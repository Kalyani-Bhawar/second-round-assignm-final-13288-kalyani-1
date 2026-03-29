package com.ecommerce.controller;

import com.ecommerce.model.Cart;
import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Cart> getCart(Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(cartService.getCartByUser(user));
    }

    @PostMapping("/add")
    public ResponseEntity<Cart> addItemToCart(Authentication authentication, @RequestBody CartRequest cartRequest) {
        User user = getUser(authentication);
        return ResponseEntity.ok(cartService.addItemToCart(user, cartRequest.getProductId(), cartRequest.getQuantity()));
    }

    @PutMapping("/update")
    public ResponseEntity<Cart> updateItemQuantity(Authentication authentication, @RequestBody CartRequest cartRequest) {
        User user = getUser(authentication);
        return ResponseEntity.ok(cartService.updateItemQuantity(user, cartRequest.getProductId(), cartRequest.getQuantity()));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Cart> removeItemFromCart(Authentication authentication, @PathVariable Long productId) {
        User user = getUser(authentication);
        return ResponseEntity.ok(cartService.removeItemFromCart(user, productId));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(Authentication authentication) {
        User user = getUser(authentication);
        cartService.clearCart(user);
        return ResponseEntity.ok().build();
    }

    private User getUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public static class CartRequest {
        private Long productId;
        private Integer quantity;
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
