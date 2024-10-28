package com.iyzico.challenge.service;

import com.iyzico.challenge.dto.FlightDetailsDto;
import com.iyzico.challenge.dto.FlightDto;
import com.iyzico.challenge.dto.SeatDto;
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
    private final SeatService seatService;

    public FlightService(FlightRepository flightRepository, Validator validator, SeatService seatService) {
        this.flightRepository = flightRepository;
        this.validator = validator;
        this.seatService = seatService;
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

    public FlightDetailsDto getFlightDetails(Long id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flight not found with id: " + id));
        
        List<SeatDto> availableSeats = seatService.getAvailableSeatsForFlight(id);
        
        FlightDetailsDto detailsDto = new FlightDetailsDto();
        detailsDto.setFlightNumber(flight.getFlightNumber());
        detailsDto.setDescription(flight.getDeparture() + " to " + flight.getArrival());
        detailsDto.setAvailableSeats(availableSeats);
        detailsDto.setPrice(flight.getPrice());
        
        return detailsDto;
    }

    public boolean canAddSeatToFlight(Long flightId) {
        Flight flight = flightRepository.findById(flightId)
            .orElseThrow(() -> new RuntimeException("Flight not found with id: " + flightId));
        int currentSeatsCount = seatService.getSeatsCountForFlight(flightId);
        return currentSeatsCount < flight.getCapacity();
    }

    public boolean flightExists(Long id) {
        return flightRepository.existsById(id);
    }

    public Flight getFlight(Long flightId) {
        return flightRepository.findById(flightId)
                .orElse(null);
    }
}
