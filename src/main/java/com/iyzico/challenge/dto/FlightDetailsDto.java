package com.iyzico.challenge.dto;

import java.math.BigDecimal;
import java.util.List;

public class FlightDetailsDto {
    private String flightNumber;
    private String description;
    private List<SeatDto> availableSeats;
    private BigDecimal price;

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<SeatDto> getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(List<SeatDto> availableSeats) {
        this.availableSeats = availableSeats;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
