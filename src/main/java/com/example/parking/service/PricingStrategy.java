package com.example.parking.service;

import com.example.parking.entity.PricingRule;
import com.example.parking.entity.VehicleType;
import com.example.parking.exception.ParkingException;
import com.example.parking.repository.PricingRuleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PricingStrategy {

    private final PricingRuleRepository ruleRepo;

    public PricingStrategy(PricingRuleRepository ruleRepo) {
        this.ruleRepo = ruleRepo;
    }

    public BigDecimal calculateFare(VehicleType type, long minutes) {
        PricingRule rule = ruleRepo.findByVehicleType(type)
                .orElseThrow(() -> new ParkingException("No pricing rule for type: " + type, 400));

        if (minutes <= rule.getFreeMinutes()) {
            return BigDecimal.ZERO;
        }

        long chargeableMinutes = minutes - rule.getFreeMinutes();
        long hours = (long) Math.ceil(chargeableMinutes / 60.0);
        return BigDecimal.valueOf(hours * rule.getRatePerHour());
    }
}
