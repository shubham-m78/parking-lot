package com.example.parking.service;

import com.example.parking.dto.EntryRequest;
import com.example.parking.dto.ExitRequest;
import com.example.parking.dto.ReceiptResponse;
import com.example.parking.dto.TicketResponse;
import com.example.parking.entity.*;
import com.example.parking.exception.ParkingException;
import com.example.parking.repository.ParkingSlotRepository;
import com.example.parking.repository.PaymentRepository;
import com.example.parking.repository.TicketRepository;
import com.example.parking.repository.VehicleRepository;
import com.example.parking.util.SlotDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.PriorityBlockingQueue;

@Service
public class ParkingService {

    private final ParkingSlotRepository slotRepo;
    private final TicketRepository ticketRepo;
    private final VehicleRepository vehicleRepo;
    private final PaymentRepository paymentRepo;
    private final Map<Gate, Map<VehicleType, PriorityBlockingQueue<SlotDistance>>> gateHeaps;
    private final Map<String, Integer> distanceMap;
    private final PricingStrategy pricingStrategy;

    @Autowired
    public ParkingService(ParkingSlotRepository slotRepo, TicketRepository ticketRepo,
                          VehicleRepository vehicleRepo, PaymentRepository paymentRepo, Map<Gate, Map<VehicleType, PriorityBlockingQueue<SlotDistance>>> gateHeaps, Map<String, Integer> distanceMap, PricingStrategy pricingStrategy) {
        this.slotRepo = slotRepo;
        this.ticketRepo = ticketRepo;
        this.vehicleRepo = vehicleRepo;
        this.paymentRepo = paymentRepo;
        this.gateHeaps = gateHeaps;
        this.distanceMap = distanceMap;
        this.pricingStrategy = pricingStrategy;
    }

    @Transactional
    public TicketResponse enterVehicle(EntryRequest req) {
        // 1. Duplicate check
        Optional<Ticket> active = ticketRepo.findActiveTicketByPlate(req.getPlateNo());
        if (active.isPresent()) {
            throw new ParkingException("Vehicle already inside", 409); // 409 Conflict
        }

        // 2. Find and lock a free slot (pessimistic)
        Gate entryGate = Gate.valueOf(req.getEntryGate());
        ParkingSlot parkingSlot = allocateSlot(entryGate, req.getType());

        if (Objects.isNull(parkingSlot)) {
            throw new ParkingException("Parking full for vehicle type: " + req.getType(), 409);
        }

        // 3. Create or fetch vehicle
        Vehicle vehicle = vehicleRepo.findByPlateNo(req.getPlateNo())
                .orElseGet(() -> vehicleRepo.save(new Vehicle(req.getPlateNo(), req.getType(), req.getOwnerName())));

        // 4. Create ticket
        Ticket ticket = new Ticket();
        ticket.setVehicle(vehicle);
        ticket.setSlot(parkingSlot);
        ticket.setEntryTime(LocalDateTime.now());
        ticket.setStatus(TicketStatus.ACTIVE);
        ticketRepo.save(ticket);

        return new TicketResponse(ticket.getId(), vehicle.getPlateNo(), parkingSlot.getSlotNumber(), ticket.getEntryTime());
    }

    @Transactional
    public ReceiptResponse exitVehicle(ExitRequest req) {
        Ticket ticket = ticketRepo.findById(req.getTicketId())
                .orElseThrow(() -> new ParkingException("Ticket not found", 400));
        if (ticket.getStatus() != TicketStatus.ACTIVE) {
            throw new ParkingException("Ticket not active", 400);
        }

        // Calculate an amount based on Pricing Rules
        LocalDateTime now = LocalDateTime.now();
        long minutes = Duration.between(ticket.getEntryTime(), now).toMinutes();
        BigDecimal amount = pricingStrategy.calculateFare(ticket.getVehicle().getType(), minutes);

        // Simulate payment (synchronous)
        Payment payment = new Payment();
        payment.setTicket(ticket);
        payment.setAmount(amount);
        payment.setTimestamp(LocalDateTime.now());
        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepo.save(payment);

        // Update ticket and free slot
        ticket.setExitTime(now);
        ticket.setStatus(TicketStatus.CLOSED);
        ticketRepo.save(ticket);

        releaseSlot(ticket);

        ReceiptResponse receiptResponse = new ReceiptResponse();
        receiptResponse.setTicketId(ticket.getId());
        receiptResponse.setAmount(amount);
        receiptResponse.setExitTime(now);
        receiptResponse.setPaymentStatus(payment.getStatus()
                .name());
        receiptResponse.setSlotFreed(true);
        return receiptResponse;
    }

    /**
     * Allocates nearest free slot for given gate & vehicle type
     */
    public synchronized ParkingSlot allocateSlot(Gate gate, VehicleType type) {
        PriorityBlockingQueue<SlotDistance> gateHeap = gateHeaps.get(gate)
                .get(type);

        while (!gateHeap.isEmpty()) {
            SlotDistance slotDistance = gateHeap.poll();
            ParkingSlot parkingSlot = slotDistance.getSlot();

            // Double-check status in DB
            Optional<ParkingSlot> fresh = slotRepo.findById(parkingSlot.getId());
            if (fresh.isPresent() && fresh.get()
                    .getStatus() == SlotStatus.FREE) {
                parkingSlot.setStatus(SlotStatus.OCCUPIED);
                slotRepo.save(parkingSlot);

                // Remove from all heaps (so other gates won't allocate it)
                removeFromOtherHeaps(parkingSlot);
                return parkingSlot;
            }
        }
        return null; // No slot available
    }

    /**
     * Remove slot from all heaps after allocation
     */
    private void removeFromOtherHeaps(ParkingSlot slot) {
        for (Gate gate : Gate.values()) {
            PriorityBlockingQueue<SlotDistance> pq = gateHeaps.get(gate)
                    .get(slot.getType());
            pq.removeIf(sd -> sd.getSlot()
                    .getId()
                    .equals(slot.getId()));
        }
    }

    /**
     * Frees a slot and re-adds to all gate heaps
     */
    public synchronized void releaseSlot(Ticket ticket) {
        ParkingSlot parkingSlot = ticket.getSlot();
        parkingSlot.setStatus(SlotStatus.FREE);
        slotRepo.save(parkingSlot);

        for (Gate gate : Gate.values()) {
            int distance = distanceMap.getOrDefault(gate.name() + "_" + parkingSlot.getSlotNumber(), Integer.MAX_VALUE);
            gateHeaps.get(gate)
                    .get(parkingSlot.getType())
                    .offer(new SlotDistance(parkingSlot, distance));
        }
    }
}
