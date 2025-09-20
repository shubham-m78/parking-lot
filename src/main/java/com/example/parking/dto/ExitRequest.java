package com.example.parking.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExitRequest {
    private Long ticketId;
    private String paymentMethod;
}
