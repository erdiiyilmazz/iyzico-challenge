package com.iyzico.challenge.dto;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PaymentRequestDto {
    @NotNull
    private Long flightId;

    @NotNull
    private Long seatId;

    @NotNull
    private String passengerName;

    @NotNull
    private BigDecimal price;

    public Long getFlightId() {
        return flightId;
    }

    public void setFlightId(Long flightId) {
        this.flightId = flightId;
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
