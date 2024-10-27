package com.iyzico.challenge.controller.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyzico.challenge.controller.PaymentController;
import com.iyzico.challenge.dto.PaymentRequestDto;
import com.iyzico.challenge.dto.PaymentResponseDto;
import com.iyzico.challenge.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    private PaymentRequestDto validPaymentRequest;
    private PaymentResponseDto successfulPaymentResponse;

    @BeforeEach
    void setUp() {
        validPaymentRequest = new PaymentRequestDto();
        validPaymentRequest.setFlightId(1L);
        validPaymentRequest.setSeatId(1L);
        validPaymentRequest.setPassengerName("John Doe");
        validPaymentRequest.setPrice(new BigDecimal("100.00"));

        successfulPaymentResponse = new PaymentResponseDto(
            1L,                        
            new BigDecimal("800.00"),  
            "200",                     
            "Test User",                
            1L,                       
            "TK123",                   
            1L,                        
            "A1"                       
        );
    }

    @Test
    void whenValidRequest_thenReturnsSuccess() throws Exception {
        when(paymentService.processSeatPurchase(any(PaymentRequestDto.class)))
            .thenReturn(successfulPaymentResponse);

        mockMvc.perform(post("/api/payments/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPaymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.price").value(800.0))  
                .andExpect(jsonPath("$.bankResponse").value("200"))
                .andExpect(jsonPath("$.passengerName").value("Test User"))
                .andExpect(jsonPath("$.flightNumber").value("TK123"))
                .andExpect(jsonPath("$.seatNumber").value("A1"));

        verify(paymentService).processSeatPurchase(any(PaymentRequestDto.class));
    }

    @Test
    void whenMissingRequiredFields_thenReturnsBadRequest() throws Exception {
        PaymentRequestDto invalidRequest = new PaymentRequestDto();

        mockMvc.perform(post("/api/payments/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(paymentService, never()).processSeatPurchase(any());
    }

    @Test
    void whenSeatNotAvailable_thenReturnsBadRequest() throws Exception {
        when(paymentService.processSeatPurchase(any(PaymentRequestDto.class)))
            .thenThrow(new IllegalStateException("Seat is not available"));

        mockMvc.perform(post("/api/payments/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPaymentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Seat is not available"));
    }

    @Test
    void whenPriceMismatch_thenReturnsBadRequest() throws Exception {
        when(paymentService.processSeatPurchase(any(PaymentRequestDto.class)))
            .thenThrow(new IllegalStateException("Payment amount does not match flight price"));

        mockMvc.perform(post("/api/payments/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPaymentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Payment amount does not match flight price"));
    }

    @Test
    void whenPaymentFails_thenReturnsInternalServerError() throws Exception {
        when(paymentService.processSeatPurchase(any(PaymentRequestDto.class)))
            .thenThrow(new RuntimeException("Payment failed"));

        mockMvc.perform(post("/api/payments/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPaymentRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An error occurred: Payment failed"));
    }

    @Test
    void whenInvalidPassengerName_thenReturnsBadRequest() throws Exception {
        validPaymentRequest.setPassengerName("");

        mockMvc.perform(post("/api/payments/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPaymentRequest)))
                .andExpect(status().isBadRequest());

        verify(paymentService, never()).processSeatPurchase(any());
    }

    @Test
    void whenInvalidPrice_thenReturnsBadRequest() throws Exception {
        validPaymentRequest.setPrice(null);

        mockMvc.perform(post("/api/payments/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPaymentRequest)))
                .andExpect(status().isBadRequest());

        verify(paymentService, never()).processSeatPurchase(any());
    }

    @Test
    void whenNegativePrice_thenReturnsBadRequest() throws Exception {
        validPaymentRequest.setPrice(new BigDecimal("-100.00"));

        mockMvc.perform(post("/api/payments/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPaymentRequest)))
                .andExpect(status().isBadRequest());

        verify(paymentService, never()).processSeatPurchase(any());
    }
}
