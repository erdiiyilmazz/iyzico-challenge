package com.iyzico.challenge.service;

import com.iyzico.challenge.dto.IyzicoPaymentRequest;
import com.iyzico.challenge.entity.Flight;
import com.iyzico.challenge.exception.PaymentProcessingException;
import com.iyzipay.Options;
import com.iyzipay.model.Payment;
import com.iyzipay.model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
class IyzicoPaymentProcessorServiceTest {

    @Mock
    private Options options;

    @Mock
    private FlightService flightService;

    @Mock
    private SeatService seatService;

    @Mock
    private Payment payment;

    private IyzicoPaymentProcessorService paymentProcessorService;

    @BeforeEach
    void setUp() {
        paymentProcessorService = new IyzicoPaymentProcessorService(options, flightService, seatService);
    }

    @Test
    void payWithIyzico_Success() {
        IyzicoPaymentRequest request = createSamplePaymentRequest();
        Flight flight = new Flight();
        flight.setFlightNumber("FL123");

        when(flightService.getFlight(request.getFlightId())).thenReturn(flight);
        when(seatService.isSeatAvailable(request.getFlightId(), request.getSeatNumber())).thenReturn(true);
        when(payment.getStatus()).thenReturn("SUCCESS"); 

        try (MockedStatic<Payment> paymentMockedStatic = mockStatic(Payment.class)) {
            paymentMockedStatic.when(() -> Payment.create(any(), any())).thenReturn(payment);
            
            String result = paymentProcessorService.payWithIyzico(request);
            assertEquals("SUCCESS", result);
        }
    }

    @Test
    void payWithIyzico_PaymentFailed() {
        IyzicoPaymentRequest request = createSamplePaymentRequest();
        Flight flight = new Flight();
        flight.setFlightNumber("FL123");

        when(flightService.getFlight(request.getFlightId())).thenReturn(flight);
        when(seatService.isSeatAvailable(request.getFlightId(), request.getSeatNumber())).thenReturn(true);
        when(payment.getStatus()).thenReturn(Status.FAILURE.getValue());

        try (MockedStatic<Payment> paymentMockedStatic = mockStatic(Payment.class)) {
            paymentMockedStatic.when(() -> Payment.create(any(), any())).thenReturn(payment);

            String result = paymentProcessorService.payWithIyzico(request);

            assertEquals("Payment failed", result);
            verify(seatService, never()).reserveSeat(any(), any());
        }
    }

    @Test
    void payWithIyzico_FlightNotFound() {
        IyzicoPaymentRequest request = createSamplePaymentRequest();
        when(flightService.getFlight(request.getFlightId())).thenReturn(null);

        PaymentProcessingException exception = assertThrows(PaymentProcessingException.class, 
            () -> paymentProcessorService.payWithIyzico(request));
        assertEquals("Payment failed", exception.getMessage());
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals("Flight not found", exception.getCause().getMessage());
    }

    @Test
    void payWithIyzico_SeatNotAvailable() {
        IyzicoPaymentRequest request = createSamplePaymentRequest();
        Flight flight = new Flight();
        flight.setFlightNumber("FL123");

        when(flightService.getFlight(request.getFlightId())).thenReturn(flight);
        when(seatService.isSeatAvailable(request.getFlightId(), request.getSeatNumber())).thenReturn(false);

        PaymentProcessingException exception = assertThrows(PaymentProcessingException.class, 
            () -> paymentProcessorService.payWithIyzico(request));
        assertEquals("Payment failed", exception.getMessage());
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals("Seat is not available", exception.getCause().getMessage());
    }

    @Test
    void payWithIyzico_PaymentProcessingException() {
        IyzicoPaymentRequest request = createSamplePaymentRequest();
        Flight flight = new Flight();
        flight.setFlightNumber("FL123");

        when(flightService.getFlight(request.getFlightId())).thenReturn(flight);
        when(seatService.isSeatAvailable(request.getFlightId(), request.getSeatNumber())).thenReturn(true);

        try (MockedStatic<Payment> paymentMockedStatic = mockStatic(Payment.class)) {
            paymentMockedStatic.when(() -> Payment.create(any(), any()))
                .thenThrow(new RuntimeException("Payment processing failed"));

            PaymentProcessingException exception = assertThrows(PaymentProcessingException.class,
                () -> paymentProcessorService.payWithIyzico(request));
            assertEquals("Payment failed", exception.getMessage());
            assertTrue(exception.getCause() instanceof RuntimeException);
            assertEquals("Payment processing failed", exception.getCause().getMessage());
        }
    }

    private IyzicoPaymentRequest createSamplePaymentRequest() {
        IyzicoPaymentRequest request = new IyzicoPaymentRequest();
        request.setFlightId(1L);
        request.setSeatNumber("A1");
        request.setCardHolderName("John Doe");
        request.setCardNumber("5528790000000008");
        request.setExpireMonth("12");
        request.setExpireYear("2030");
        request.setCvc("123");
        request.setPrice(BigDecimal.valueOf(100.0));
        return request;
    }
}

