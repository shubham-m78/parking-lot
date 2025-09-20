package com.example.parking.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "parking_slot",
        uniqueConstraints = @UniqueConstraint(columnNames = {"slotNumber"})
)
@Data
@NoArgsConstructor
public class ParkingSlot {

    public ParkingSlot(String slotNumber, int floorNumber, VehicleType type, SlotStatus status) {
        this.slotNumber = slotNumber;
        this.floorNumber = floorNumber;
        this.type = type;
        this.status = status;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String slotNumber;

    private int floorNumber;

    @Enumerated(EnumType.STRING)
    private VehicleType type;

    @Enumerated(EnumType.STRING)
    private SlotStatus status;

    @Version
    private Long version;
}
