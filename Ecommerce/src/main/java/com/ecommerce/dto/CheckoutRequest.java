package com.ecommerce.dto;

public class CheckoutRequest {
    private String shippingAddress;

    public CheckoutRequest() {}

    public CheckoutRequest(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}
