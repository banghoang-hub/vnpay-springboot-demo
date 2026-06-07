package com.example.vnpaydemo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class PaymentRequest {
    @NotNull(message = "Vui lòng nhập số tiền")
    @Min(value = 1000, message = "Số tiền tối thiểu là 1.000 VND")
    private Long amount;

    private String orderInfo;

    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    public String getOrderInfo() { return orderInfo; }
    public void setOrderInfo(String orderInfo) { this.orderInfo = orderInfo; }
}
