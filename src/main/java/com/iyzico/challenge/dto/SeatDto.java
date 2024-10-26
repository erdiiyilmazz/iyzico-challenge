package com.iyzico.challenge.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class SeatDto {
    private Long id;

    @NotNull(message = "Flight ID cannot be null")
    private Long flightId;

    @NotNull(message = "Seat number cannot be null")
    @Pattern(regexp = "[A-K]([1-9]|[1-4][0-9]|50)", message = "Seat number must be one uppercase letter (A-K) followed by a number (1-50)")
    private String seatNumber;

    private boolean isAvailable = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFlightId() {
        return flightId;
    }

    public void setFlightId(Long flightId) {
        this.flightId = flightId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}
