package com.example.parking.controller;

import com.example.parking.entity.PricingRule;
import com.example.parking.repository.PricingRuleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/pricing")
public class AdminPricingController {

    private final PricingRuleRepository repo;

    public AdminPricingController(PricingRuleRepository repo) {
        this.repo = repo;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<PricingRule> getAllRules() {
        return repo.findAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<PricingRule> createOrUpdate(@RequestBody PricingRule rule) {
        // If rule exists for this vehicle type, update instead of inserting new
        return ResponseEntity.ok(repo.save(rule));
    }
}
