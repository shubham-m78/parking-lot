package com.example.parking.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class FareResponse {
    private Long ticketId;
    private String plateNo;
    private long durationMinutes;
    private BigDecimal amount;
}