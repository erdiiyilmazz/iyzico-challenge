package com.iyzico.challenge.controller;

import com.iyzico.challenge.dto.IyzicoPaymentRequest;
import com.iyzico.challenge.dto.PaymentRequestDto;
import com.iyzico.challenge.dto.PaymentResponseDto;
import com.iyzico.challenge.service.IyzicoPaymentProcessorService;
import com.iyzico.challenge.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    private final IyzicoPaymentProcessorService paymentProcessorService;

    public PaymentController(PaymentService paymentService, IyzicoPaymentProcessorService paymentProcessorService) {
        this.paymentService = paymentService;
        this.paymentProcessorService = paymentProcessorService;
    }

    @PostMapping("/purchase")
    public ResponseEntity<?> purchaseSeat(@Valid @RequestBody PaymentRequestDto paymentRequest) {
        try {
            PaymentResponseDto paymentResponseDto = paymentService.processSeatPurchase(paymentRequest);
            return ResponseEntity.ok(paymentResponseDto);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body("An error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/purchase-with-iyzico/{flightId}/{seatNumber}")
    public ResponseEntity<String> purchaseSeatWithIyzico(
            @PathVariable Long flightId,
            @PathVariable String seatNumber,
            @Valid @RequestBody IyzicoPaymentRequest paymentRequest) {
        paymentRequest.setFlightId(flightId);
        paymentRequest.setSeatNumber(seatNumber);
        String paymentStatus = paymentProcessorService.payWithIyzico(paymentRequest);
        return ResponseEntity.ok(paymentStatus);
    }

}
