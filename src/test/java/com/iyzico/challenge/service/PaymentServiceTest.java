package com.iyzico.challenge.service;

import com.iyzico.challenge.dto.PaymentRequestDto;
import com.iyzico.challenge.dto.PaymentResponseDto;
import com.iyzico.challenge.entity.Flight;
import com.iyzico.challenge.entity.Seat;
import com.iyzico.challenge.repository.FlightRepository;
import com.iyzico.challenge.repository.PaymentRepository;
import com.iyzico.challenge.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BankService bankService;

    @InjectMocks
    private PaymentService paymentService;

    private Flight testFlight;
    private Seat testSeat;
    private PaymentRequestDto validPaymentRequest;

    @BeforeEach
    void setUp() {
        testFlight = new Flight();
        testFlight.setId(1L);
        testFlight.setFlightNumber("TK123");
        testFlight.setPrice(new BigDecimal("100.00"));

        testSeat = new Seat();
        testSeat.setId(1L);
        testSeat.setFlight(testFlight);
        testSeat.setSeatNumber("A1");
        testSeat.setAvailable(true);

        validPaymentRequest = new PaymentRequestDto();
        validPaymentRequest.setFlightId(1L);
        validPaymentRequest.setSeatId(1L);
        validPaymentRequest.setPassengerName("Test User");
        validPaymentRequest.setPrice(new BigDecimal("100.00"));
    }

    @Test
    void whenValidPaymentRequest_thenSuccess() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(testFlight));
        when(seatRepository.findById(1L)).thenReturn(Optional.of(testSeat));
        when(bankService.pay(any())).thenReturn(new BankPaymentResponse("200"));
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PaymentResponseDto response = paymentService.processSeatPurchase(validPaymentRequest);

        assertNotNull(response);
        assertEquals("TK123", response.getFlightNumber());
        assertEquals("A1", response.getSeatNumber());
        verify(seatRepository).save(any());
        verify(paymentRepository).save(any());
    }

    @Test
    void whenSeatNotAvailable_thenThrowException() {
        testSeat.setAvailable(false);
        when(flightRepository.findById(1L)).thenReturn(Optional.of(testFlight));
        when(seatRepository.findById(1L)).thenReturn(Optional.of(testSeat));

        assertThrows(IllegalStateException.class, () -> 
            paymentService.processSeatPurchase(validPaymentRequest)
        );
        verify(bankService, never()).pay(any());
    }

    @Test
    void whenPriceMismatch_thenThrowException() {
        validPaymentRequest.setPrice(new BigDecimal("200.00"));
        when(flightRepository.findById(1L)).thenReturn(Optional.of(testFlight));
        when(seatRepository.findById(1L)).thenReturn(Optional.of(testSeat));

        assertThrows(IllegalStateException.class, () -> 
            paymentService.processSeatPurchase(validPaymentRequest)
        );
        verify(bankService, never()).pay(any());
    }

    @Test
    void whenBankPaymentFails_thenThrowException() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(testFlight));
        when(seatRepository.findById(1L)).thenReturn(Optional.of(testSeat));
        when(bankService.pay(any())).thenReturn(new BankPaymentResponse("400"));

        assertThrows(RuntimeException.class, () -> 
            paymentService.processSeatPurchase(validPaymentRequest)
        );
        verify(seatRepository, never()).save(any());
    }

    @Test
    void testConcurrentPaymentForSameSeat() throws InterruptedException {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(testFlight));
        when(seatRepository.findById(1L)).thenReturn(Optional.of(testSeat));
        when(bankService.pay(any())).thenReturn(new BankPaymentResponse("200"));
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        int numberOfThreads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                try {
                    paymentService.processSeatPurchase(validPaymentRequest);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        assertEquals(1, successCount.get(), "Only one payment should succeed");
        assertEquals(numberOfThreads - 1, failureCount.get(), "All other payments should fail");
    }

    @Test
    void whenFlightNotFound_thenThrowException() {
        when(flightRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            paymentService.processSeatPurchase(validPaymentRequest)
        );
    }

    @Test
    void whenSeatNotFound_thenThrowException() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(testFlight));
        when(seatRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            paymentService.processSeatPurchase(validPaymentRequest)
        );
    }
}
