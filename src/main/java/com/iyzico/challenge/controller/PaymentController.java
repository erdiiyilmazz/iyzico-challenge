package com.iyzico.challenge.controller;

import com.iyzico.challenge.dto.PaymentRequestDto;
import com.iyzico.challenge.dto.PaymentResponseDto;
import com.iyzico.challenge.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
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
}
