package com.ecommerce.dto;

public class PaymentStatusRequest {
    private String status;

    public PaymentStatusRequest() {}

    public PaymentStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
