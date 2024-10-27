package com.iyzico.challenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyzico.challenge.dto.FlightDto;
import com.iyzico.challenge.service.FlightService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootTest
@AutoConfigureMockMvc
public class FlightControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FlightService flightService;

    private FlightDto validFlightDto;

    @BeforeEach
    void setUp() {
        validFlightDto = new FlightDto();
        validFlightDto.setFlightNumber("AB123");
        validFlightDto.setDeparture("IST");
        validFlightDto.setArrival("LHR");
        validFlightDto.setDepartureTime(LocalDateTime.now().plusDays(1));
        validFlightDto.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(4));
        validFlightDto.setCapacity(200);
        validFlightDto.setPrice(new BigDecimal("800.00"));
  }

    @Test
    public void testCreateFlight() throws Exception {
        FlightDto flightDto = new FlightDto();
        flightDto.setFlightNumber("TK123");
        flightDto.setDeparture("IST");
        flightDto.setArrival("LHR");
        flightDto.setDepartureTime(LocalDateTime.now().plusDays(1));
        flightDto.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(4));
        flightDto.setCapacity(200);
        flightDto.setPrice(new BigDecimal("800.00"));

        mockMvc.perform(post("/api/flights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(flightDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flightNumber").value("TK123"))
                .andExpect(jsonPath("$.capacity").value(200));
    }

    @Test
    public void testGetFlight() throws Exception {
        FlightDto createdFlight = flightService.addFlight(validFlightDto);

        mockMvc.perform(get("/api/flights/{id}", createdFlight.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("AB123"))
                .andExpect(jsonPath("$.capacity").value(200));
    }

    @Test
    public void testUpdateFlight() throws Exception {
        FlightDto createdFlight = flightService.addFlight(validFlightDto);
        
        FlightDto updatedFlightDto = new FlightDto();
        updatedFlightDto.setFlightNumber("TK999");
        updatedFlightDto.setDeparture("IST");
        updatedFlightDto.setArrival("LHR");
        updatedFlightDto.setDepartureTime(LocalDateTime.now().plusDays(1));
        updatedFlightDto.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(4));
        updatedFlightDto.setCapacity(200);
        updatedFlightDto.setPrice(new BigDecimal("900.00"));

        mockMvc.perform(put("/api/flights/{id}", createdFlight.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedFlightDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("TK999"))
                .andExpect(jsonPath("$.capacity").value(200));
    }

    @Test
    public void testDeleteFlight() throws Exception {
        FlightDto createdFlight = flightService.addFlight(validFlightDto);

        mockMvc.perform(delete("/api/flights/{id}", createdFlight.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/flights/{id}", createdFlight.getId()))
                .andExpect(status().isNotFound());
    }
}
