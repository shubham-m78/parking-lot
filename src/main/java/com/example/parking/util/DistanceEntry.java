package com.example.parking.util;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DistanceEntry {
    private String gateNumber;
    private String slotNumber;
    private int distance;
}