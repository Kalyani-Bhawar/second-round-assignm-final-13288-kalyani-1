package com.ecommerce.controller;

import com.ecommerce.dto.CartRequest;
import com.ecommerce.model.Cart;
import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
        Cart cart = cartService.getCartByUser(user);
        
        // Security check: User can only see their own cart
        validateCartOwnership(user, cart);
        
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/add")
    public ResponseEntity<Cart> addItemToCart(Authentication authentication, @RequestBody CartRequest cartRequest) {
        User user = getUser(authentication);
        Cart cart = cartService.addItemToCart(user, cartRequest.getProductId(), cartRequest.getQuantity());
        
        // Security check
        validateCartOwnership(user, cart);
        
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/update")
    public ResponseEntity<Cart> updateItemQuantity(Authentication authentication, @RequestBody CartRequest cartRequest) {
        User user = getUser(authentication);
        Cart cart = cartService.updateItemQuantity(user, cartRequest.getProductId(), cartRequest.getQuantity());
        
        // Security check
        validateCartOwnership(user, cart);
        
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Cart> removeItemFromCart(Authentication authentication, @PathVariable Long productId) {
        User user = getUser(authentication);
        Cart cart = cartService.removeItemFromCart(user, productId);
        
        // Security check
        validateCartOwnership(user, cart);
        
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(Authentication authentication) {
        User user = getUser(authentication);
        Cart cart = cartService.getCartByUser(user);
        
        // Security check
        validateCartOwnership(user, cart);
        
        cartService.clearCart(user);
        return ResponseEntity.ok().build();
    }

    private User getUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void validateCartOwnership(User user, Cart cart) {
        if (!cart.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to access this cart");
        }
    }
}

