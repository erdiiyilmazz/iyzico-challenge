package com.iyzico.challenge.controller.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyzico.challenge.controller.SeatController;
import com.iyzico.challenge.dto.SeatDto;
import com.iyzico.challenge.service.SeatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.iyzico.challenge.exception.ResourceNotFoundException;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SeatController.class)
public class SeatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SeatService seatService;

    private SeatDto validSeatDto;

    @BeforeEach
    void setUp() {
        validSeatDto = new SeatDto();
        validSeatDto.setId(1L);
        validSeatDto.setFlightId(1L);
        validSeatDto.setSeatNumber("A1");
        validSeatDto.setAvailable(true);
    }

    @Test
    void test_createSeat_success() throws Exception {
        when(seatService.addSeat(any(SeatDto.class))).thenReturn(validSeatDto);

        mockMvc.perform(post("/api/seats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validSeatDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.seatNumber").value("A1"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void test_createSeat_failureWithInvalidData() throws Exception {
        when(seatService.addSeat(any(SeatDto.class)))
                .thenThrow(new IllegalStateException("Invalid seat data"));

        mockMvc.perform(post("/api/seats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validSeatDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_updateSeat_success() throws Exception {
        when(seatService.updateSeat(eq(1L), any(SeatDto.class))).thenReturn(validSeatDto);

        mockMvc.perform(put("/api/seats/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validSeatDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seatNumber").value("A1"));
    }

    @Test
    void test_updateSeat_failureWithInvalidData() throws Exception {
        when(seatService.updateSeat(eq(1L), any(SeatDto.class)))
                .thenThrow(new IllegalStateException("Invalid seat data"));

        mockMvc.perform(put("/api/seats/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validSeatDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_deleteSeat_success() throws Exception {
        doNothing().when(seatService).deleteSeat(1L);

        mockMvc.perform(delete("/api/seats/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void test_deleteSeat_failureWithNonExistentId() throws Exception {
        doThrow(new ResourceNotFoundException("Seat not found"))
            .when(seatService).deleteSeat(999L);

        mockMvc.perform(delete("/api/seats/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void test_getSeatsForFlight_success() throws Exception {
        when(seatService.getSeatsByFlightId(1L))
                .thenReturn(Arrays.asList(validSeatDto));

        mockMvc.perform(get("/api/seats/flight/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].seatNumber").value("A1"))
                .andExpect(jsonPath("$[0].available").value(true));
    }

    @Test
    void test_createSeat_failureWithInvalidSeatNumber() throws Exception {
        validSeatDto.setSeatNumber("Invalid");
        
        mockMvc.perform(post("/api/seats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validSeatDto)))
                .andExpect(status().isBadRequest());
    }
}
