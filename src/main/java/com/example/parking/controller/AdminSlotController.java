package com.example.parking.controller;

import com.example.parking.entity.ParkingSlot;
import com.example.parking.entity.SlotStatus;
import com.example.parking.repository.ParkingSlotRepository;
import com.example.parking.service.HeapManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/slots")
public class AdminSlotController {

    private final ParkingSlotRepository repo;
    private final HeapManager heapManager;

    public AdminSlotController(ParkingSlotRepository repo, HeapManager heapManager) {
        this.repo = repo;
        this.heapManager = heapManager;
    }

    // Get all slots
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<ParkingSlot> getAllSlots() {
        return repo.findAll();
    }

    // Add a new slot
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> addSlot(@RequestBody ParkingSlot slot) {
        if (repo.findBySlotNumber(slot.getSlotNumber())
                .isPresent()) {
            return ResponseEntity.badRequest()
                    .body("Slot with number " + slot.getSlotNumber() + " already exists");
        }
        slot.setStatus(SlotStatus.FREE);
        ParkingSlot saved = repo.save(slot);

        heapManager.rebuildHeaps(repo.findAll());  // keep heaps in sync

        return ResponseEntity.ok(saved);
    }

    // Update slot (e.g., change type or floor)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ParkingSlot> updateSlot(@PathVariable Long id, @RequestBody ParkingSlot updated) {
        return repo.findById(id)
                .map(slot -> {
                    slot.setSlotNumber(updated.getSlotNumber());
                    slot.setFloorNumber(updated.getFloorNumber());
                    slot.setType(updated.getType());
                    slot.setStatus(updated.getStatus());
                    ParkingSlot saved = repo.save(slot);

                    // Keep heaps in sync
                    heapManager.rebuildHeaps(repo.findAll());

                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound()
                        .build());
    }


    // Delete slot
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSlot(@PathVariable Long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);

            // Keep heaps in sync
            heapManager.rebuildHeaps(repo.findAll());

            return ResponseEntity.noContent()
                    .build();
        }
        return ResponseEntity.notFound()
                .build();
    }

}
