package com.iyzico.challenge.controller.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyzico.challenge.dto.FlightDto;
import com.iyzico.challenge.dto.SeatDto;
import com.iyzico.challenge.dto.PaymentRequestDto;
import com.iyzico.challenge.service.FlightService;
import com.iyzico.challenge.service.SeatService;
import com.iyzico.challenge.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private FlightService flightService;
    
    @Autowired
    private SeatService seatService;
    
    @Autowired
    private PaymentService paymentService;

    private SeatDto testSeat;
    private PaymentRequestDto validPaymentRequest;

    @BeforeEach
    void setUp() {
        FlightDto testFlight = new FlightDto();
        testFlight.setFlightNumber("TK123");
        testFlight.setDeparture("IST");
        testFlight.setArrival("LHR");
        testFlight.setDepartureTime(LocalDateTime.now().plusDays(1));
        testFlight.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(4));
        testFlight.setCapacity(100);
        testFlight.setPrice(new BigDecimal("100.00"));
        
        testFlight = flightService.addFlight(testFlight);

        testSeat = new SeatDto();
        testSeat.setFlightId(testFlight.getId());
        testSeat.setSeatNumber("A1");
        testSeat.setAvailable(true);
        
        testSeat = seatService.addSeat(testSeat);

        validPaymentRequest = new PaymentRequestDto();
        validPaymentRequest.setFlightId(testFlight.getId());
        validPaymentRequest.setSeatId(testSeat.getId());
        validPaymentRequest.setPassengerName("Erdi Yilmaz");
        validPaymentRequest.setPrice(testFlight.getPrice());
    }

    @Test
    void whenValidRequest_thenSuccessfulPayment() throws Exception {
        mockMvc.perform(post("/api/payments/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPaymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("TK123"))
                .andExpect(jsonPath("$.seatNumber").value("A1"))
                .andExpect(jsonPath("$.passengerName").value("Erdi Yilmaz"))
                .andExpect(jsonPath("$.price").value(100.0))  
                .andReturn();

        SeatDto updatedSeat = seatService.getSeatById(testSeat.getId());
        assertFalse(updatedSeat.isAvailable());
    }

    @Test
    void whenPriceMismatch_thenPaymentFails() throws Exception {
        validPaymentRequest.setPrice(new BigDecimal("200.00"));

        mockMvc.perform(post("/api/payments/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPaymentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Payment amount does not match flight price"));

        SeatDto updatedSeat = seatService.getSeatById(testSeat.getId());
        assertTrue(updatedSeat.isAvailable());
    }

    @Test
    void whenSeatAlreadyTaken_thenPaymentFails() throws Exception {
        mockMvc.perform(post("/api/payments/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPaymentRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/payments/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPaymentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Seat is not available"));
    }

    @Test
    void testConcurrentPaymentRequests() throws Exception {
        TestData testData = createTestData();
        
        int numberOfThreads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        PaymentRequestDto paymentRequest = new PaymentRequestDto();
        paymentRequest.setFlightId(testData.flightId);
        paymentRequest.setSeatId(testData.seatId);
        paymentRequest.setPassengerName("Test User");
        paymentRequest.setPrice(testData.price);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                try {
                    MvcResult result = mockMvc.perform(post("/api/payments/purchase")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(paymentRequest)))
                            .andReturn();

                    String content = result.getResponse().getContentAsString();
                    int status = result.getResponse().getStatus();
                    
                    if (status == 200) {
                        successCount.incrementAndGet();
                    } else if (status == 400 && content.contains("Seat is not available")) {
                        failureCount.incrementAndGet();
                    } else {
                        System.err.println("Unexpected response: " + status + " - " + content);
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    System.err.println("Error during concurrent test: " + e.getMessage());
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

        SeatDto finalSeatState = seatService.getSeatById(testData.seatId);
        assertFalse(finalSeatState.isAvailable(), "Seat should be marked as unavailable");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    TestData createTestData() {
        FlightDto testFlight = new FlightDto();
        testFlight.setFlightNumber("TK123");
        testFlight.setDeparture("IST");
        testFlight.setArrival("LHR");
        testFlight.setDepartureTime(LocalDateTime.now().plusDays(1));
        testFlight.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(4));
        testFlight.setCapacity(100);
        testFlight.setPrice(new BigDecimal("100.00"));
        
        FlightDto savedFlight = flightService.addFlight(testFlight);

        SeatDto testSeat = new SeatDto();
        testSeat.setFlightId(savedFlight.getId());
        testSeat.setSeatNumber("A1");
        testSeat.setAvailable(true);
        
        SeatDto savedSeat = seatService.addSeat(testSeat);

        return new TestData(savedFlight.getId(), savedSeat.getId(), savedFlight.getPrice());
    }

    private static class TestData {
        final Long flightId;
        final Long seatId;
        final BigDecimal price;

        TestData(Long flightId, Long seatId, BigDecimal price) {
            this.flightId = flightId;
            this.seatId = seatId;
            this.price = price;
        }
    }

    @Test
    void whenInvalidFlightId_thenPaymentFails() throws Exception {
        validPaymentRequest.setFlightId(999L);

        mockMvc.perform(post("/api/payments/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPaymentRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Flight not found")));
    }

    @Test
    void whenInvalidSeatId_thenPaymentFails() throws Exception {
        validPaymentRequest.setSeatId(999L);

        mockMvc.perform(post("/api/payments/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPaymentRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Seat not found")));
    }

    @Test
    void whenMissingRequiredFields_thenPaymentFails() throws Exception {
        PaymentRequestDto invalidRequest = new PaymentRequestDto();

        mockMvc.perform(post("/api/payments/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
