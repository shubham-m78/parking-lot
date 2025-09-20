package com.example.parking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vehicle", uniqueConstraints = @UniqueConstraint(columnNames = {"plate_no"}))
@Data
@NoArgsConstructor
public class Vehicle {

    public Vehicle(String plateNo, VehicleType type, String ownerName) {
        this.plateNo = plateNo;
        this.type = type;
        this.ownerName = ownerName;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plate_no", nullable = false)
    private String plateNo;

    @Enumerated(EnumType.STRING)
    private VehicleType type;

    private String ownerName;
}
