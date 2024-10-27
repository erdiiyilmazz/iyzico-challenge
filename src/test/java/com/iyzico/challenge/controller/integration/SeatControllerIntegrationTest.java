package com.iyzico.challenge.controller.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyzico.challenge.dto.SeatDto;
import com.iyzico.challenge.dto.FlightDto;
import com.iyzico.challenge.service.SeatService;
import com.iyzico.challenge.service.FlightService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SeatControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SeatService seatService;

    @Autowired
    private FlightService flightService;

    private SeatDto validSeatDto;

    @BeforeEach
    void setUp() {
        FlightDto validFlightDto = new FlightDto();
        validFlightDto.setFlightNumber("AB123");
        validFlightDto.setDeparture("IST");
        validFlightDto.setArrival("LHR");
        validFlightDto.setDepartureTime(LocalDateTime.now().plusDays(1));
        validFlightDto.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(4));
        validFlightDto.setCapacity(200);
        validFlightDto.setPrice(new BigDecimal("800.00"));
        
        FlightDto createdFlight = flightService.addFlight(validFlightDto);

        validSeatDto = new SeatDto();
        validSeatDto.setFlightId(createdFlight.getId());
        validSeatDto.setSeatNumber("A1");
        validSeatDto.setAvailable(true);
    }

    @Test
    public void test_createSeat_success() throws Exception {
        mockMvc.perform(post("/api/seats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validSeatDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.seatNumber").value("A1"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    public void test_getSeat_success() throws Exception {
        SeatDto createdSeat = seatService.addSeat(validSeatDto);

        mockMvc.perform(get("/api/seats/{id}", createdSeat.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seatNumber").value("A1"));
    }

    @Test
    public void test_updateSeat_success() throws Exception {
        SeatDto createdSeat = seatService.addSeat(validSeatDto);
        validSeatDto.setAvailable(false);

        mockMvc.perform(put("/api/seats/{id}", createdSeat.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validSeatDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    public void test_deleteSeat_success() throws Exception {
        SeatDto createdSeat = seatService.addSeat(validSeatDto);

        mockMvc.perform(delete("/api/seats/{id}", createdSeat.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/seats/{id}", createdSeat.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void test_getSeatsForFlight_success() throws Exception {
        seatService.addSeat(validSeatDto);

        mockMvc.perform(get("/api/seats/flight/{flightId}", validSeatDto.getFlightId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].seatNumber").value("A1"));
    }
}
