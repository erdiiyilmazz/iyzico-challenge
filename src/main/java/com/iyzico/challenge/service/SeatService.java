package com.iyzico.challenge.service;

import com.iyzico.challenge.dto.SeatDto;
import com.iyzico.challenge.entity.Flight;
import com.iyzico.challenge.entity.Seat;
import com.iyzico.challenge.repository.FlightRepository;
import com.iyzico.challenge.repository.SeatRepository;
import com.iyzico.challenge.util.FlightValidation;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.iyzico.challenge.exception.ResourceNotFoundException;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SeatService {

    private final SeatRepository seatRepository;
    private final FlightRepository flightRepository;

    public SeatService(SeatRepository seatRepository, FlightRepository flightRepository) {
        this.seatRepository = seatRepository;
        this.flightRepository = flightRepository;
    }

    @Transactional
    public SeatDto addSeat(SeatDto seatDto) {
        Flight flight = flightRepository.findById(seatDto.getFlightId())
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));
        
        if (flight.getSeats().size() >= flight.getCapacity()) {
            throw new IllegalStateException("Cannot add more seats. Flight capacity reached.");
        }
        
        FlightValidation.validateSeatNumber(seatDto.getSeatNumber());
        
        Optional<Seat> existingSeat = seatRepository.findByFlightAndSeatNumber(flight, seatDto.getSeatNumber());
        
        if (existingSeat.isPresent()) {
            throw new IllegalStateException("Seat number " + seatDto.getSeatNumber() + " already exists for this flight");
        } else {
            Seat seat = new Seat(flight, seatDto.getSeatNumber());
            seat.setAvailable(seatDto.isAvailable());
            Seat savedSeat = seatRepository.save(seat);
            return convertToDto(savedSeat);
        }
    }

    @Transactional
    public SeatDto updateSeat(Long id, SeatDto seatDto) {
        Seat existingSeat = seatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Seat not found"));
        
        FlightValidation.validateSeatNumber(seatDto.getSeatNumber());
        
        if (!existingSeat.getSeatNumber().equals(seatDto.getSeatNumber())) {
            Optional<Seat> seatWithNewNumber = seatRepository.findByFlightAndSeatNumber(existingSeat.getFlight(), seatDto.getSeatNumber());
            if (seatWithNewNumber.isPresent()) {
                throw new IllegalStateException("The new seat number is already taken");
            }
        }
        
        existingSeat.setSeatNumber(seatDto.getSeatNumber());
        existingSeat.setAvailable(seatDto.isAvailable());
        
        Seat updatedSeat = seatRepository.save(existingSeat);
        return convertToDto(updatedSeat);
    }

    @Transactional
    public void deleteSeat(Long id) {
        Seat seat = seatRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + id));
        seatRepository.delete(seat);
    }

    public List<SeatDto> getAvailableSeatsForFlight(Long flightId) {
        return seatRepository.findByFlightIdAndIsAvailableTrue(flightId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public int getSeatsCountForFlight(Long flightId) {
        return seatRepository.countByFlightId(flightId);
    }

    public SeatDto getSeatById(Long id) {
        Seat seat = seatRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Seat not found"));
        return convertToDto(seat);
    }

    public List<SeatDto> getSeatsByFlightId(Long flightId) {
        Flight flight = flightRepository.findById(flightId)
            .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));
        return seatRepository.findByFlightId(flightId).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    private SeatDto convertToDto(Seat seat) {
        SeatDto dto = new SeatDto();
        BeanUtils.copyProperties(seat, dto);
        dto.setFlightId(seat.getFlight().getId());
        dto.setAvailable(seat.isAvailable());  
        return dto;
    }

    public boolean isSeatAvailable(Long flightId, String seatNumber) {
        Flight flight = flightRepository.findById(flightId)
            .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));
            
        Optional<Seat> seat = seatRepository.findByFlightAndSeatNumber(flight, seatNumber);
        
        return seat.map(Seat::isAvailable)
            .orElseThrow(() -> new ResourceNotFoundException("Seat not found"));
    }

    @Transactional
    public void reserveSeat(Long flightId, String seatNumber) {
        Flight flight = flightRepository.findById(flightId)
            .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));
            
        Seat seat = seatRepository.findByFlightAndSeatNumber(flight, seatNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Seat not found"));
            
        if (!seat.isAvailable()) {
            throw new IllegalStateException("Seat is already reserved");
        }
        
        seat.setAvailable(false);
        seatRepository.save(seat);
    }
}
