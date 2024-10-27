package com.iyzico.challenge.dto;

import java.math.BigDecimal;

public class PaymentResponseDto {
    private Long id;
    private BigDecimal price;
    private String bankResponse;
    private String passengerName;
    private Long flightId;
    private String flightNumber;
    private Long seatId;
    private String seatNumber;

    public PaymentResponseDto() {}

    public PaymentResponseDto(Long id, BigDecimal price, String bankResponse, String passengerName,
                              Long flightId, String flightNumber, Long seatId, String seatNumber) {
        this.id = id;
        this.price = price;
        this.bankResponse = bankResponse;
        this.passengerName = passengerName;
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.seatId = seatId;
        this.seatNumber = seatNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public Long getFlightId() {
        return flightId;
    }

    public void setFlightId(Long flightId) {
        this.flightId = flightId;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }
}
