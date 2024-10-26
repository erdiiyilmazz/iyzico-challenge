package com.iyzico.challenge.controller;

import com.iyzico.challenge.dto.FlightDetailsDto;
import com.iyzico.challenge.dto.FlightDto;
import com.iyzico.challenge.service.FlightService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) { 
        this.flightService = flightService;
    }

    @PostMapping
    public ResponseEntity<?> addFlight(@Valid @RequestBody FlightDto flightDTO) {
        try {
            FlightDto savedFlight = flightService.addFlight(flightDTO);
            return new ResponseEntity<>(savedFlight, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFlight(@PathVariable Long id, @Valid @RequestBody FlightDto flightDTO) {
        try {
            FlightDto updatedFlight = flightService.updateFlight(id, flightDTO);
            return ResponseEntity.ok(updatedFlight);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlight(@PathVariable Long id) {
        flightService.deleteFlight(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFlightById(@PathVariable Long id) {
        try {
            FlightDto flight = flightService.getFlightById(id);
            return ResponseEntity.ok(flight);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<FlightDto>> getAllFlights() {
        List<FlightDto> flights = flightService.getAllFlights();
        return ResponseEntity.ok(flights);
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<?> getFlightDetails(@PathVariable Long id) {
        try {
            FlightDetailsDto flightDetails = flightService.getFlightDetails(id);
            return ResponseEntity.ok(flightDetails);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
