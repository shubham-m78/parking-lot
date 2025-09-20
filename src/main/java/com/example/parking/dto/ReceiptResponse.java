package com.example.parking.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ReceiptResponse {
    private Long ticketId;
    private BigDecimal amount;
    private String paymentStatus;
    private LocalDateTime exitTime;
    private boolean slotFreed;
}
