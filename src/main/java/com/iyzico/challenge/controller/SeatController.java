package com.iyzico.challenge.controller;

import com.iyzico.challenge.dto.SeatDto;
import com.iyzico.challenge.service.SeatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/seats")
@Validated
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @PostMapping
    public ResponseEntity<?> addSeat(@Valid @RequestBody SeatDto seatDto) {
        try {
            SeatDto savedSeat = seatService.addSeat(seatDto);
            return new ResponseEntity<>(savedSeat, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSeat(@PathVariable Long id, @Valid @RequestBody SeatDto seatDto) {
        try {
            SeatDto updatedSeat = seatService.updateSeat(id, seatDto);
            return ResponseEntity.ok(updatedSeat);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSeat(@PathVariable Long id) {
        try {
            seatService.deleteSeat(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
