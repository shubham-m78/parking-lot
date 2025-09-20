package com.example.parking.service;

import com.example.parking.entity.Gate;
import com.example.parking.entity.ParkingSlot;
import com.example.parking.entity.SlotStatus;
import com.example.parking.entity.VehicleType;
import com.example.parking.repository.ParkingSlotRepository;
import com.example.parking.util.SlotDistance;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

@Service
public class HeapManager {

    private final Map<Gate, Map<VehicleType, PriorityBlockingQueue<SlotDistance>>> gateHeaps =
            new ConcurrentHashMap<>();

    private final Map<String, Integer> distanceMap;

    public HeapManager(Map<String, Integer> distanceMap) {
        this.distanceMap = distanceMap;
    }

    public synchronized void rebuildHeaps(List<ParkingSlot> slots) {
        gateHeaps.clear();
        for (Gate gate : Gate.values()) {
            gateHeaps.putIfAbsent(gate, new ConcurrentHashMap<>());
            for (VehicleType type : VehicleType.values()) {
                PriorityBlockingQueue<SlotDistance> pq = new PriorityBlockingQueue<>();
                slots.stream()
                        .filter(slot -> slot.getType() == type && slot.getStatus() == SlotStatus.FREE)
                        .forEach(slot -> pq.offer(new SlotDistance(slot,
                                precomputedDistance(gate, slot.getSlotNumber()))));
                gateHeaps.get(gate).put(type, pq);
            }
        }
    }

    public Map<Gate, Map<VehicleType, PriorityBlockingQueue<SlotDistance>>> getGateHeaps() {
        return gateHeaps;
    }

    private int precomputedDistance(Gate gate, String slotNumber) {
        return distanceMap.getOrDefault(gate.name() + "_" + slotNumber, Integer.MAX_VALUE);
    }
}
