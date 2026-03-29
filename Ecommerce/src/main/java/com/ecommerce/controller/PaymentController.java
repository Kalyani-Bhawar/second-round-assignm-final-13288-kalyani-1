package com.ecommerce.controller;

import com.ecommerce.model.Order;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    @PostMapping("/create-payment-intent/{orderId}")
    public ResponseEntity<Map<String, String>> createPaymentIntent(@PathVariable Long orderId) throws StripeException {
        Order order = orderService.getOrderById(orderId);
        PaymentIntent intent = paymentService.createPaymentIntent(order);
        
        Map<String, String> responseData = new HashMap<>();
        responseData.put("clientSecret", intent.getClientSecret());
        
        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/confirm/{orderId}")
    public ResponseEntity<?> confirmPayment(@PathVariable Long orderId, @RequestBody PaymentConfirmationRequest confirmationRequest) {
        // In a real application, this would be handled via Stripe Webhooks
        // For this assignment, we'll provide a manual confirmation endpoint
        if ("SUCCESS".equals(confirmationRequest.getStatus())) {
            orderService.updatePaymentStatus(orderId, "PAID");
            return ResponseEntity.ok("Payment confirmed and order updated.");
        } else {
            orderService.updatePaymentStatus(orderId, "FAILED");
            return ResponseEntity.badRequest().body("Payment failed.");
        }
    }

    public static class PaymentConfirmationRequest {
        private String status;
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
