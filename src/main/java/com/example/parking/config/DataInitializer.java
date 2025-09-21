package com.example.parking.config;

import com.example.parking.entity.*;
import com.example.parking.repository.ParkingSlotRepository;
import com.example.parking.repository.PricingRuleRepository;
import com.example.parking.service.HeapManager;
import com.example.parking.util.SlotDistance;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Adds sample parking slots on startup for testing.
 */
@Configuration
public class DataInitializer {

    private final Map<String, Integer> distanceMap;

    // Map<Gate, Map<VehicleType, MinHeap>>
    private final Map<Gate, Map<VehicleType, PriorityBlockingQueue<SlotDistance>>> gateHeaps =
            new ConcurrentHashMap<>();

    public DataInitializer(Map<String, Integer> distanceMap) {
        this.distanceMap = distanceMap;
    }

    @Bean
    public Map<Gate, Map<VehicleType, PriorityBlockingQueue<SlotDistance>>> gateHeaps() {
        return gateHeaps;
    }

    @Bean
    CommandLineRunner init(ParkingSlotRepository parkingSlotRepository, PricingRuleRepository pricingRuleRepository, HeapManager heapManager) {
        return args -> {
            // 1. Create a set of slots & pricing rules
            initializeParkingSlots(parkingSlotRepository);

            pricingRuleRepository.save(new PricingRule(VehicleType.BIKE, 1, 10));
            pricingRuleRepository.save(new PricingRule(VehicleType.CAR, 1, 20));
            pricingRuleRepository.save(new PricingRule(VehicleType.TRUCK, 1, 30));


            // 2. Fetch all slots
            List<ParkingSlot> slots = parkingSlotRepository.findAll();

            // 3. Build a heap per gate
            for (Gate gate : Gate.values()) {
                gateHeaps.putIfAbsent(gate, new ConcurrentHashMap<>());
                for (VehicleType type : VehicleType.values()) {
                    PriorityBlockingQueue<SlotDistance> pq = new PriorityBlockingQueue<>();
                    slots.stream()
                            .filter(slot -> slot.getType() == type && slot.getStatus() == SlotStatus.FREE)
                            .forEach(slot -> pq.offer(new SlotDistance(slot, precomputedDistance(gate, slot.getSlotNumber()))));
                    gateHeaps.get(gate)
                            .put(type, pq);
                }
            }
        };
    }

    // Precomputed distance function
    public int precomputedDistance(Gate gate, String slotNumber) {
        if (!distanceMap.containsKey(gate.name() + "_" + slotNumber)) {
            throw new IllegalStateException("Missing distance for " + gate + " -> " + slotNumber);
        }

        return distanceMap.getOrDefault(gate.name() + "_" + slotNumber, Integer.MAX_VALUE);
    }

    private void initializeParkingSlots(ParkingSlotRepository repo) {
// Floor 1 - 6 slots
        repo.save(new ParkingSlot("F1-01", 1, VehicleType.CAR, SlotStatus.FREE));
        repo.save(new ParkingSlot("F1-02", 1, VehicleType.CAR, SlotStatus.FREE));
        repo.save(new ParkingSlot("F1-03", 1, VehicleType.BIKE, SlotStatus.FREE));
        repo.save(new ParkingSlot("F1-04", 1, VehicleType.BIKE, SlotStatus.FREE));
        repo.save(new ParkingSlot("F1-05", 1, VehicleType.TRUCK, SlotStatus.FREE));
        repo.save(new ParkingSlot("F1-06", 1, VehicleType.TRUCK, SlotStatus.FREE));

// Floor 2 - 7 slots
        repo.save(new ParkingSlot("F2-01", 2, VehicleType.CAR, SlotStatus.FREE));
        repo.save(new ParkingSlot("F2-02", 2, VehicleType.CAR, SlotStatus.FREE));
        repo.save(new ParkingSlot("F2-03", 2, VehicleType.CAR, SlotStatus.FREE));
        repo.save(new ParkingSlot("F2-04", 2, VehicleType.BIKE, SlotStatus.FREE));
        repo.save(new ParkingSlot("F2-05", 2, VehicleType.BIKE, SlotStatus.FREE));
        repo.save(new ParkingSlot("F2-06", 2, VehicleType.TRUCK, SlotStatus.FREE));
        repo.save(new ParkingSlot("F2-07", 2, VehicleType.TRUCK, SlotStatus.FREE));

// Floor 3 - 5 slots
        repo.save(new ParkingSlot("F3-01", 3, VehicleType.CAR, SlotStatus.FREE));
        repo.save(new ParkingSlot("F3-02", 3, VehicleType.CAR, SlotStatus.FREE));
        repo.save(new ParkingSlot("F3-03", 3, VehicleType.BIKE, SlotStatus.FREE));
        repo.save(new ParkingSlot("F3-04", 3, VehicleType.TRUCK, SlotStatus.FREE));
        repo.save(new ParkingSlot("F3-05", 3, VehicleType.TRUCK, SlotStatus.FREE));

// Floor 4 - 8 slots
        repo.save(new ParkingSlot("F4-01", 4, VehicleType.CAR, SlotStatus.FREE));
        repo.save(new ParkingSlot("F4-02", 4, VehicleType.CAR, SlotStatus.FREE));
        repo.save(new ParkingSlot("F4-03", 4, VehicleType.CAR, SlotStatus.FREE));
        repo.save(new ParkingSlot("F4-04", 4, VehicleType.BIKE, SlotStatus.FREE));
        repo.save(new ParkingSlot("F4-05", 4, VehicleType.BIKE, SlotStatus.FREE));
        repo.save(new ParkingSlot("F4-06", 4, VehicleType.BIKE, SlotStatus.FREE));
        repo.save(new ParkingSlot("F4-07", 4, VehicleType.TRUCK, SlotStatus.FREE));
        repo.save(new ParkingSlot("F4-08", 4, VehicleType.TRUCK, SlotStatus.FREE));
    }
}
