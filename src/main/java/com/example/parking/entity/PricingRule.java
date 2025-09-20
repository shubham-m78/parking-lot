package com.example.parking.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pricing_rule")
@Data
@NoArgsConstructor
public class PricingRule {

    public PricingRule(VehicleType vehicleType, int freeMinutes, int ratePerHour) {
        this.vehicleType = vehicleType;
        this.freeMinutes = freeMinutes;
        this.ratePerHour = ratePerHour;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private VehicleType vehicleType;

    private int freeMinutes;     // e.g., 120 for 2 hours free
    private int ratePerHour;     // ₹ per hour after free minutes
}
