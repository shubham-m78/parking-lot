package com.example.parking.dto;

import com.example.parking.entity.VehicleType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EntryRequest {
    private String plateNo;
    private VehicleType type;
    private String entryGate;
    private String ownerName;
}
