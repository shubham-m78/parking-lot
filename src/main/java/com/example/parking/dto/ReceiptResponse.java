package com.example.parking.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ReceiptResponse {
    private Long ticketId;
    private String plateNo;
    private String slotNumber;
    private LocalDateTime exitTime;
    private String paymentStatus;
    private BigDecimal paidAmount;
    private BigDecimal remainingChange;
    private boolean slotFreed;
    private String message;
}
