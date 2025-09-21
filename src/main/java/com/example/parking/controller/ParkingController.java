package com.example.parking.controller;

import com.example.parking.dto.*;
import com.example.parking.service.ParkingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/parking")
public class ParkingController {

    @Autowired
    private ParkingService parkingService;

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/entry")
    public ResponseEntity<TicketResponse> entry(@RequestBody EntryRequest req) {
        TicketResponse ticketResponse = parkingService.enterVehicle(req);
        return ResponseEntity.ok(ticketResponse);
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/fare/{ticketId}")
    public ResponseEntity<FareResponse> calculateFare(@PathVariable Long ticketId) {
        FareResponse response = parkingService.calculateFare(ticketId);
        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/exit")
    public ResponseEntity<ReceiptResponse> exit(@RequestBody ExitRequest exitRequest) {
        ReceiptResponse receiptResponse = parkingService.exitVehicle(exitRequest);
        return ResponseEntity.ok(receiptResponse);
    }
}
