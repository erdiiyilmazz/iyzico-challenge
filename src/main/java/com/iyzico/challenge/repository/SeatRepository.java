package com.iyzico.challenge.repository;

import com.iyzico.challenge.entity.Seat;
import com.iyzico.challenge.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByFlightIdAndIsAvailableTrue(Long flightId);

    Optional<Seat> findByFlightAndSeatNumber(Flight flight, String seatNumber);

    List<Seat> findByFlightId(Long flightId);

    int countByFlightId(Long flightId);
}
