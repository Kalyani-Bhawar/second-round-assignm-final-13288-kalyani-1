package com.ecommerce.service;

import com.ecommerce.model.Order;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    @Value("${stripe.api.key:}")
    private String stripeSecretKey;

    @Autowired
    private OrderService orderService;

    @Autowired(required = false)
    public void validateStripeKey() {
        if (stripeSecretKey == null || stripeSecretKey.trim().isEmpty() || stripeSecretKey.contains("placeholder")) {
            logger.warn("Stripe API key is not properly configured. Please set 'stripe.api.key' environment variable with a valid secret key.");
        }
    }

    public PaymentIntent createPaymentIntent(Order order) throws StripeException {
        if (stripeSecretKey == null || stripeSecretKey.trim().isEmpty()) {
            throw new RuntimeException("Stripe API key is not configured. Please set the 'stripe.api.key' environment variable.");
        }
        
        Stripe.apiKey = stripeSecretKey;

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(order.getTotalPrice().multiply(new java.math.BigDecimal(100)).longValue())
                .setCurrency("usd")
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .putMetadata("order_id", order.getId().toString())
                .build();

        return PaymentIntent.create(params);
    }
}
