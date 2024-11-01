package com.iyzico.challenge.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "seats")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "flight_id", nullable = false)
    @NotNull
    private Flight flight;

    @Column(nullable = false)
    @NotNull
    private String seatNumber;

    @Column(nullable = false)
    @NotNull
    private Boolean isAvailable;

    public Seat() {
    }

    public Seat(@NotNull Flight flight, @NotNull String seatNumber) {
        this.flight = flight;
        this.seatNumber = seatNumber;
        this.isAvailable = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Flight getFlight() {
        return flight;
    }

    public void setFlight(Flight flight) {
        this.flight = flight;
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
