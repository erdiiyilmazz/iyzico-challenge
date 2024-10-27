package com.iyzico.challenge.service;

import com.iyzico.challenge.dto.SeatDto;
import com.iyzico.challenge.entity.Seat;
import com.iyzico.challenge.entity.Flight;
import com.iyzico.challenge.repository.SeatRepository;
import com.iyzico.challenge.repository.FlightRepository;
import com.iyzico.challenge.exception.ResourceNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class SeatServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private SeatService seatService;

    private Seat testSeat;
    private SeatDto testSeatDto;
    private Flight testFlight;

    @BeforeEach
    void setUp() {
        testFlight = new Flight();
        testFlight.setId(1L);
        
        testSeat = new Seat();
        testSeat.setId(1L);
        testSeat.setSeatNumber("A1");
        testSeat.setAvailable(true);
        testSeat.setFlight(testFlight);

        testSeatDto = new SeatDto();
        testSeatDto.setId(1L);
        testSeatDto.setSeatNumber("A1");
        testSeatDto.setAvailable(true);
        testSeatDto.setFlightId(1L);
    }

    @Test
    void test_createSeat_success() {
        Flight flight = new Flight();
        flight.setId(1L);
        flight.setCapacity(100);
        
        SeatDto seatDto = new SeatDto();
        seatDto.setFlightId(1L);
        seatDto.setSeatNumber("A1");
        
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(seatRepository.findByFlightAndSeatNumber(any(), any())).thenReturn(Optional.empty());
        when(seatRepository.save(any())).thenReturn(new Seat(flight, "A1"));
        
        SeatDto result = seatService.addSeat(seatDto);
        
        assertNotNull(result);
        assertEquals("A1", result.getSeatNumber());
    }

    @Test
    void test_createSeat_failureWithNonExistentFlight() {
        when(flightRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            seatService.addSeat(testSeatDto);  
        });
    }

    @Test
    void test_getSeat_success() {
        when(seatRepository.findById(1L)).thenReturn(Optional.of(testSeat));

        SeatDto result = seatService.getSeatById(1L);

        assertNotNull(result);
        assertEquals("A1", result.getSeatNumber());
    }

    @Test
    void test_updateSeat_success() {
        when(seatRepository.findById(1L)).thenReturn(Optional.of(testSeat));
        when(seatRepository.save(any(Seat.class))).thenReturn(testSeat);

        testSeatDto.setAvailable(false);
        SeatDto result = seatService.updateSeat(1L, testSeatDto);

        assertNotNull(result);
        assertFalse(result.isAvailable());
    }

    @Test
    void test_deleteSeat_success() {
        when(seatRepository.findById(1L)).thenReturn(Optional.of(testSeat));
        doNothing().when(seatRepository).delete(any(Seat.class));

        seatService.deleteSeat(1L);

        verify(seatRepository).delete(testSeat);
    }

    @Test
    void test_getSeatsForFlight_success() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(testFlight));
        when(seatRepository.findByFlightId(1L)).thenReturn(Arrays.asList(testSeat));

        List<SeatDto> results = seatService.getSeatsByFlightId(1L);

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals("A1", results.get(0).getSeatNumber());
    }

    @Test
    void test_getSeatsForFlight_failureWithNonExistentFlight() {
        when(flightRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            seatService.getSeatsByFlightId(999L);
        });
    }
}
