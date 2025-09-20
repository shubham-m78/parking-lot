package com.example.parking.util;

import com.example.parking.entity.ParkingSlot;
import lombok.Getter;

@Getter
public class SlotDistance implements Comparable<SlotDistance> {
    private final ParkingSlot slot;
    private final int distance;

    public SlotDistance(ParkingSlot slot, int distance) {
        this.slot = slot;
        this.distance = distance;
    }

    @Override
    public int compareTo(SlotDistance other) {
        return Integer.compare(this.distance, other.distance);
    }
}
