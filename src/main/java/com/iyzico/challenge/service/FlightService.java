package com.iyzico.challenge.service;

import com.iyzico.challenge.dto.FlightDto;
import com.iyzico.challenge.entity.Flight;
import com.iyzico.challenge.repository.FlightRepository;
import com.iyzico.challenge.util.FlightValidation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.validation.Valid;
import javax.validation.Validator;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FlightService {

    private final FlightRepository flightRepository;
    private final Validator validator;

    public FlightService(FlightRepository flightRepository, Validator validator) {
        this.flightRepository = flightRepository;
        this.validator = validator;
    }

    @Transactional
    public FlightDto addFlight(@Valid FlightDto flightDTO) {
        FlightValidation.validateFlightDto(flightDTO, validator);
        FlightValidation.validateFlightTimes(flightDTO);
        FlightValidation.validateFlightNumber(flightDTO.getFlightNumber());
        Flight flight = FlightValidation.convertToEntity(flightDTO);
        Flight savedFlight = flightRepository.save(flight);
        return FlightValidation.convertToDTO(savedFlight);
    }

    @Transactional
    public FlightDto updateFlight(Long id, @Valid FlightDto flightDTO) {
        FlightValidation.validateFlightDto(flightDTO, validator);
        FlightValidation.validateFlightTimes(flightDTO);
        FlightValidation.validateFlightNumber(flightDTO.getFlightNumber());
        Flight existingFlight = flightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flight not found"));
        
        Flight updatedFlight = FlightValidation.convertToEntity(flightDTO);
        updatedFlight.setId(existingFlight.getId());
        updatedFlight = flightRepository.save(updatedFlight);
        return FlightValidation.convertToDTO(updatedFlight);
    }

    @Transactional
    public void deleteFlight(Long id) {
        flightRepository.deleteById(id);
    }

    public List<FlightDto> getAllFlights() {
        return flightRepository.findAll().stream()
                .map(FlightValidation::convertToDTO)
                .collect(Collectors.toList());
    }

    public FlightDto getFlightById(Long id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flight not found with id: " + id));
        return FlightValidation.convertToDTO(flight);
    }

    public boolean isFlightFull(Long id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flight not found with id: " + id));
        return FlightValidation.isFlightFull(flight);
    }
}
