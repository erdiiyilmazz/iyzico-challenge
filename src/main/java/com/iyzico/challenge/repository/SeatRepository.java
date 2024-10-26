package com.iyzico.challenge.repository;

import com.iyzico.challenge.entity.Seat;
import com.iyzico.challenge.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    Optional<Seat> findByFlightAndSeatNumber(Flight flight, String seatNumber);
}
