package com.iyzico.challenge.service;

import com.iyzico.challenge.dto.PaymentRequestDto;
import com.iyzico.challenge.dto.PaymentResponseDto;
import com.iyzico.challenge.entity.Flight;
import com.iyzico.challenge.entity.Seat;
import com.iyzico.challenge.entity.Payment;
import com.iyzico.challenge.repository.FlightRepository;
import com.iyzico.challenge.repository.SeatRepository;
import com.iyzico.challenge.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class PaymentService {

    private final FlightRepository flightRepository;
    private final SeatRepository seatRepository;
    private final PaymentRepository paymentRepository;
    private final BankService bankService;
    private final Lock lock = new ReentrantLock();

    public PaymentService(FlightRepository flightRepository, SeatRepository seatRepository,
                          PaymentRepository paymentRepository, BankService bankService) {
        this.flightRepository = flightRepository;
        this.seatRepository = seatRepository;
        this.paymentRepository = paymentRepository;
        this.bankService = bankService;
    }

    @Transactional
    public PaymentResponseDto processSeatPurchase(PaymentRequestDto paymentRequest) {
        lock.lock();
        try {
            Flight flight = flightRepository.findById(paymentRequest.getFlightId())
                    .orElseThrow(() -> new RuntimeException("Flight not found"));

            Seat seat = seatRepository.findById(paymentRequest.getSeatId())
                    .orElseThrow(() -> new RuntimeException("Seat not found"));

            if (!seat.isAvailable()) {
                throw new IllegalStateException("Seat is not available");
            }

            if (!seat.getFlight().getId().equals(flight.getId())) {
                throw new IllegalStateException("Seat does not belong to the specified flight");
            }

            if (!flight.getPrice().equals(paymentRequest.getPrice())) {
                throw new IllegalStateException("Payment amount does not match flight price");
            }

            BankPaymentRequest bankRequest = new BankPaymentRequest();
            bankRequest.setPrice(paymentRequest.getPrice());
            BankPaymentResponse bankResponse = bankService.pay(bankRequest);

            if (bankResponse != null && "200".equals(bankResponse.getResultCode())) {
                seat.setAvailable(false);
                seatRepository.save(seat);

                Payment payment = new Payment();
                payment.setPrice(paymentRequest.getPrice());
                payment.setBankResponse(bankResponse.getResultCode());
                payment.setPassengerName(paymentRequest.getPassengerName());
                payment.setFlight(flight);
                payment.setSeat(seat);
                payment = paymentRepository.save(payment);

                return new PaymentResponseDto(
                    payment.getId(),
                    payment.getPrice(),
                    payment.getBankResponse(),
                    payment.getPassengerName(),
                    flight.getId(),
                    flight.getFlightNumber(),
                    seat.getId(),
                    seat.getSeatNumber()
                );
            } else {
                throw new RuntimeException("Payment failed");
            }
        } finally {
            lock.unlock();
        }
    }
}
