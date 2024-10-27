package com.iyzico.challenge.service;

import com.iyzico.challenge.dto.FlightDto;
import com.iyzico.challenge.entity.Flight;
import com.iyzico.challenge.repository.FlightRepository;
import com.iyzico.challenge.util.FlightValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.validation.Validator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private Validator validator;

    @Mock
    private SeatService seatService;

    @InjectMocks
    private FlightService flightService;

    private FlightDto validFlightDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        validFlightDto = new FlightDto();
        validFlightDto.setFlightNumber("AB123");
        validFlightDto.setDeparture("New York");
        validFlightDto.setArrival("London");
        validFlightDto.setDepartureTime(LocalDateTime.now().plusDays(1));
        validFlightDto.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(8));
        validFlightDto.setPrice(new BigDecimal("500.00"));
        validFlightDto.setCapacity(200);
    }

    @Test
    void testAddFlight_Success() {
        when(flightRepository.save(any(Flight.class))).thenReturn(FlightValidation.convertToEntity(validFlightDto));

        FlightDto savedFlightDto = flightService.addFlight(validFlightDto);

        assertNotNull(savedFlightDto);
        assertEquals("AB123", savedFlightDto.getFlightNumber());
        verify(flightRepository, times(1)).save(any(Flight.class));
    }

    @Test
    void testAddFlight_InvalidFlightNumber() {
        validFlightDto.setFlightNumber("123");
        assertThrows(IllegalArgumentException.class, () -> flightService.addFlight(validFlightDto));
    }

    @Test
    void testAddFlight_PastDepartureTime() {
        validFlightDto.setDepartureTime(LocalDateTime.now().minusDays(1));
        assertThrows(IllegalArgumentException.class, () -> flightService.addFlight(validFlightDto));
    }

    @Test
    void testGetFlightById_Success() {
        Long flightId = 1L;
        Flight flight = FlightValidation.convertToEntity(validFlightDto);
        flight.setId(flightId);

        when(flightRepository.findById(flightId)).thenReturn(Optional.of(flight));

        FlightDto flightDto = flightService.getFlightById(flightId);

        assertNotNull(flightDto);
        assertEquals("AB123", flightDto.getFlightNumber());
        verify(flightRepository, times(1)).findById(flightId);
    }

    @Test
    void testGetFlightById_NotFound() {
        Long flightId = 1L;
        when(flightRepository.findById(flightId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> flightService.getFlightById(flightId));
    }

    @Test
    void testUpdateFlight_Success() {
        Long flightId = 1L;
        Flight existingFlight = FlightValidation.convertToEntity(validFlightDto);
        existingFlight.setId(flightId);

        when(flightRepository.findById(flightId)).thenReturn(Optional.of(existingFlight));
        when(flightRepository.save(any(Flight.class))).thenAnswer(invocation -> invocation.getArgument(0));

        validFlightDto.setPrice(new BigDecimal("600.00"));
        FlightDto updatedFlightDto = flightService.updateFlight(flightId, validFlightDto);

        assertNotNull(updatedFlightDto);
        assertEquals(new BigDecimal("600.00"), updatedFlightDto.getPrice());
        verify(flightRepository, times(1)).save(any(Flight.class));
    }

    @Test
    void testUpdateFlight_NotFound() {
        Long flightId = 1L;
        when(flightRepository.findById(flightId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> flightService.updateFlight(flightId, validFlightDto));
    }

    @Test
    void testDeleteFlight_Success() {
        Long flightId = 1L;
        doNothing().when(flightRepository).deleteById(flightId);

        assertDoesNotThrow(() -> flightService.deleteFlight(flightId));
        verify(flightRepository, times(1)).deleteById(flightId);
    }

    @Test
    void testGetAllFlights_Success() {
        List<Flight> flights = Arrays.asList(
            FlightValidation.convertToEntity(validFlightDto),
            FlightValidation.convertToEntity(validFlightDto)
        );
        when(flightRepository.findAll()).thenReturn(flights);

        List<FlightDto> flightDtos = flightService.getAllFlights();

        assertNotNull(flightDtos);
        assertEquals(2, flightDtos.size());
        verify(flightRepository, times(1)).findAll();
    }

    @Test
    void testCanAddSeatToFlight_Success() {
        Long flightId = 1L;
        Flight flight = FlightValidation.convertToEntity(validFlightDto);
        flight.setId(flightId);
        flight.setCapacity(200);

        when(flightRepository.findById(flightId)).thenReturn(Optional.of(flight));
        when(seatService.getSeatsCountForFlight(flightId)).thenReturn(199);

        boolean canAddSeat = flightService.canAddSeatToFlight(flightId);

        assertTrue(canAddSeat);
    }

    @Test
    void testCanAddSeatToFlight_CapacityReached() {
        Long flightId = 1L;
        Flight flight = FlightValidation.convertToEntity(validFlightDto);
        flight.setId(flightId);
        flight.setCapacity(200);

        when(flightRepository.findById(flightId)).thenReturn(Optional.of(flight));
        when(seatService.getSeatsCountForFlight(flightId)).thenReturn(200);

        boolean canAddSeat = flightService.canAddSeatToFlight(flightId);

        assertFalse(canAddSeat);
    }
}
