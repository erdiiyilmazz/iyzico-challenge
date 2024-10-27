package com.iyzico.challenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyzico.challenge.dto.FlightDto;
import com.iyzico.challenge.dto.FlightDetailsDto;
import com.iyzico.challenge.service.FlightService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FlightController.class)
public class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FlightService flightService;

    private FlightDto validFlightDto;

    private FlightDetailsDto validFlightDetailsDto;

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

        validFlightDetailsDto = new FlightDetailsDto();
        validFlightDetailsDto.setFlightNumber("AB123");
        validFlightDetailsDto.setDescription("IST to LHR");
        validFlightDetailsDto.setPrice(new BigDecimal("800.00"));
    }

    @Test
    void testAddFlight() throws Exception {
        when(flightService.addFlight(any(FlightDto.class))).thenReturn(validFlightDto);

        mockMvc.perform(post("/api/flights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFlightDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flightNumber").value("AB123"));
    }

    @Test
    void testGetFlight() throws Exception {
        when(flightService.getFlightById(1L)).thenReturn(validFlightDto);

        mockMvc.perform(get("/api/flights/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("AB123"))
                .andExpect(jsonPath("$.departure").value("IST"))
                .andExpect(jsonPath("$.arrival").value("LHR"));
    }

    @Test
    void testUpdateFlight() throws Exception {
        when(flightService.updateFlight(eq(1L), any(FlightDto.class))).thenReturn(validFlightDto);

        mockMvc.perform(put("/api/flights/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFlightDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("AB123"));
    }

    @Test
    void testDeleteFlight() throws Exception {
        doNothing().when(flightService).deleteFlight(1L);

        mockMvc.perform(delete("/api/flights/1"))
                .andExpect(status().isNoContent());

        verify(flightService, times(1)).deleteFlight(1L);
    }

    @Test
    void testGetAllFlights() throws Exception {
        List<FlightDto> flightList = Arrays.asList(validFlightDto);
        when(flightService.getAllFlights()).thenReturn(flightList);

        mockMvc.perform(get("/api/flights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].flightNumber").value("AB123"));
    }

    @Test
    void testGetFlightDetails() throws Exception {
        when(flightService.getFlightDetails(1L)).thenReturn(validFlightDetailsDto);

        mockMvc.perform(get("/api/flights/1/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("AB123"))
                .andExpect(jsonPath("$.description").value("IST to LHR"));
    }

    @Test
    void testAddFlightInvalidInput() throws Exception {
        FlightDto invalidFlight = new FlightDto();

        mockMvc.perform(post("/api/flights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidFlight)))
                .andExpect(status().isBadRequest());
    }
}
