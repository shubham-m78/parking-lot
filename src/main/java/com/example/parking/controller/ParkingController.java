package com.example.parking.controller;

import com.example.parking.dto.EntryRequest;
import com.example.parking.dto.ExitRequest;
import com.example.parking.dto.ReceiptResponse;
import com.example.parking.dto.TicketResponse;
import com.example.parking.service.ParkingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/parking")
public class ParkingController {

    @Autowired
    private ParkingService parkingService;

    @PreAuthorize("hasRole('USER','ADMIN')")
    @PostMapping("/entry")
    public ResponseEntity<TicketResponse> entry(@RequestBody EntryRequest req) {
        TicketResponse ticketResponse = parkingService.enterVehicle(req);
        return ResponseEntity.ok(ticketResponse);
    }

    @PreAuthorize("hasRole('USER','ADMIN')")
    @PostMapping("/exit")
    public ResponseEntity<ReceiptResponse> exit(@RequestBody ExitRequest req) {
        ReceiptResponse receiptResponse = parkingService.exitVehicle(req);
        return ResponseEntity.ok(receiptResponse);
    }
}
