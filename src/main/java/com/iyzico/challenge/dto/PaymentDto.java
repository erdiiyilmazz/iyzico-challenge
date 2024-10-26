package com.iyzico.challenge.dto;

import java.math.BigDecimal;

public class PaymentDto {
    private BigDecimal price;
    private String bankResponse;

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getBankResponse() {
        return bankResponse;
    }

    public void setBankResponse(String bankResponse) {
        this.bankResponse = bankResponse;
    }
}
