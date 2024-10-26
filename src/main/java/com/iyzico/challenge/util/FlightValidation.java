package com.iyzico.challenge.util;

import com.iyzico.challenge.dto.FlightDto;
import com.iyzico.challenge.entity.Flight;
import org.springframework.beans.BeanUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public class FlightValidation {
    
    public static void validateFlightTimes(FlightDto flightDTO) {
        LocalDateTime now = LocalDateTime.now();
        if (flightDTO.getDepartureTime().isBefore(now)) {
            throw new IllegalArgumentException("Departure time must be in the future");
        }
        if (flightDTO.getArrivalTime().isBefore(now)) {
            throw new IllegalArgumentException("Arrival time must be in the future");
        }
        if (flightDTO.getDepartureTime().isAfter(flightDTO.getArrivalTime()) || 
            flightDTO.getDepartureTime().isEqual(flightDTO.getArrivalTime())) {
            throw new IllegalArgumentException("Departure time must be before arrival time");
        }
        if (flightDTO.getDeparture().equalsIgnoreCase(flightDTO.getArrival())) {
            throw new IllegalArgumentException("Departure and arrival locations cannot be the same");
        }
        if (Duration.between(flightDTO.getDepartureTime(), flightDTO.getArrivalTime()).toHours() > 24) {
            throw new IllegalArgumentException("Flight duration cannot exceed 24 hours");
        }
    }

    public static void validateFlightNumber(String flightNumber) {
        if (!flightNumber.matches("[A-Z]{2}\\d{3}")) {
            throw new IllegalArgumentException("Flight number must be 2 uppercase letters followed by 3 digits");
        }
    }

    public static void validateFlightDto(FlightDto flightDTO, Validator validator) {
        Set<ConstraintViolation<FlightDto>> violations = validator.validate(flightDTO);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", ")));
        }
    }

    public static boolean isFlightFull(Flight flight) {
        return flight.getSeats().size() >= flight.getCapacity();
    }

    public static FlightDto convertToDTO(Flight flight) {
        FlightDto dto = new FlightDto();
        BeanUtils.copyProperties(flight, dto);
        return dto;
    }

    public static Flight convertToEntity(FlightDto dto) {
        Flight flight = new Flight();
        BeanUtils.copyProperties(dto, flight);
        return flight;
    }
}
