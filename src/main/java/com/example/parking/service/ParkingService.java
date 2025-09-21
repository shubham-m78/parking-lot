package com.example.parking.service;

import com.example.parking.dto.*;
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

    private final ParkingSlotRepository parkingSlotRepository;
    private final TicketRepository ticketRepository;
    private final VehicleRepository vehicleRepository;
    private final PaymentRepository paymentRepository;
    private final Map<Gate, Map<VehicleType, PriorityBlockingQueue<SlotDistance>>> gateHeaps;
    private final Map<String, Integer> distanceMap;
    private final PricingStrategy pricingStrategy;

    @Autowired
    public ParkingService(ParkingSlotRepository parkingSlotRepository, TicketRepository ticketRepository,
                          VehicleRepository vehicleRepository, PaymentRepository paymentRepository, Map<Gate, Map<VehicleType, PriorityBlockingQueue<SlotDistance>>> gateHeaps, Map<String, Integer> distanceMap, PricingStrategy pricingStrategy) {
        this.parkingSlotRepository = parkingSlotRepository;
        this.ticketRepository = ticketRepository;
        this.vehicleRepository = vehicleRepository;
        this.paymentRepository = paymentRepository;
        this.gateHeaps = gateHeaps;
        this.distanceMap = distanceMap;
        this.pricingStrategy = pricingStrategy;
    }

    @Transactional
    public TicketResponse enterVehicle(EntryRequest entryRequest) {
        // 1. Duplicate check
        Optional<Ticket> active = ticketRepository.findActiveTicketByPlate(entryRequest.getPlateNo());
        if (active.isPresent()) {
            throw new ParkingException("Vehicle already inside", 409); // 409 Conflict
        }

        // 2. Find and lock a free slot (pessimistic)
        Gate entryGate = Gate.valueOf(entryRequest.getEntryGate());
        ParkingSlot parkingSlot = allocateSlot(entryGate, entryRequest.getVehicleType());

        if (Objects.isNull(parkingSlot)) {
            throw new ParkingException("Parking full for vehicle type: " + entryRequest.getVehicleType(), 409);
        }

        // 3. Create or fetch vehicle
        Vehicle vehicle = vehicleRepository.findByPlateNo(entryRequest.getPlateNo())
                .orElseGet(() -> vehicleRepository.save(new Vehicle(entryRequest.getPlateNo(), entryRequest.getVehicleType(), entryRequest.getOwnerName())));

        // 4. Create & Return the ticket
        return createTicket(vehicle, parkingSlot);
    }

    private TicketResponse createTicket(Vehicle vehicle, ParkingSlot parkingSlot) {
        Ticket ticket = new Ticket();
        ticket.setVehicle(vehicle);
        ticket.setSlot(parkingSlot);
        ticket.setEntryTime(LocalDateTime.now());
        ticket.setStatus(TicketStatus.ACTIVE);
        ticketRepository.save(ticket);

        return new TicketResponse(ticket.getId(), vehicle.getPlateNo(), parkingSlot.getSlotNumber(), ticket.getEntryTime());
    }

    public FareResponse calculateFare(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ParkingException("Ticket not found", 400));

        if (ticket.getStatus() != TicketStatus.ACTIVE) {
            throw new ParkingException("Ticket not active", 400);
        }

        // Calculate an amount based on Pricing Rules
        LocalDateTime now = LocalDateTime.now();
        long minutes = Duration.between(ticket.getEntryTime(), now)
                .toMinutes();
        BigDecimal amount = pricingStrategy.calculateFare(ticket.getVehicle()
                .getType(), minutes);

        FareResponse fareResponse = new FareResponse();
        fareResponse.setTicketId(ticket.getId());
        fareResponse.setPlateNo(ticket.getVehicle()
                .getPlateNo());
        fareResponse.setDurationMinutes(minutes);
        fareResponse.setAmount(amount);

        // Save the amount to be paid against the ticket with pending status
        Payment payment = new Payment();
        payment.setTicket(ticket);
        payment.setAmount(amount);
        payment.setTimestamp(LocalDateTime.now());
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        return fareResponse;
    }

    @Transactional
    public ReceiptResponse exitVehicle(ExitRequest exitRequest) {
        Ticket ticket = ticketRepository.findById(exitRequest.getTicketId())
                .orElseThrow(() -> new ParkingException("Ticket not found", 400));

        if (ticket.getStatus() != TicketStatus.ACTIVE) {
            throw new ParkingException("Ticket not active", 400);
        }

        // Find existing Payment row for this ticket
        Payment payment = paymentRepository.findByTicketId(exitRequest.getTicketId())
                .orElseThrow(() -> new ParkingException("No pending payment found for this ticket", 400));


        // Validate the amount
        if (exitRequest.getAmount()
                .compareTo(payment.getAmount()) < 0) {
            throw new ParkingException("Insufficient payment. Required: " + payment.getAmount(), 400);
        }

        // Update payment row to SUCCESS
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTimestamp(LocalDateTime.now());
        paymentRepository.save(payment);

        // Update ticket and free slot
        ticket.setExitTime(LocalDateTime.now());
        ticket.setStatus(TicketStatus.CLOSED);
        ticketRepository.save(ticket);

        releaseSlot(ticket);

        ReceiptResponse receiptResponse = new ReceiptResponse();
        receiptResponse.setTicketId(ticket.getId());
        receiptResponse.setPlateNo(ticket.getVehicle()
                .getPlateNo());
        receiptResponse.setSlotNumber(ticket.getSlot()
                .getSlotNumber());
        receiptResponse.setExitTime(LocalDateTime.now());
        receiptResponse.setPaymentStatus(payment.getStatus()
                .name());
        receiptResponse.setPaidAmount(exitRequest.getAmount());
        receiptResponse.setRemainingChange(exitRequest.getAmount()
                .subtract(payment.getAmount()));
        receiptResponse.setSlotFreed(true);
        receiptResponse.setMessage("Exit successful, visit again. Thank you!");

        return receiptResponse;
    }

    /**
     * Allocates nearest free slot for given gate & vehicle type
     */
    private synchronized ParkingSlot allocateSlot(Gate gate, VehicleType type) {
        PriorityBlockingQueue<SlotDistance> gateHeap = gateHeaps.get(gate)
                .get(type);

        while (!gateHeap.isEmpty()) {
            SlotDistance slotDistance = gateHeap.poll();
            ParkingSlot parkingSlot = slotDistance.getSlot();

            // Double-check status in DB
            Optional<ParkingSlot> fresh = parkingSlotRepository.findById(parkingSlot.getId());
            if (fresh.isPresent() && fresh.get()
                    .getStatus() == SlotStatus.FREE) {
                parkingSlot.setStatus(SlotStatus.OCCUPIED);
                parkingSlotRepository.save(parkingSlot);

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
    private void removeFromOtherHeaps(ParkingSlot parkingSlot) {
        for (Gate gate : Gate.values()) {
            PriorityBlockingQueue<SlotDistance> gateHeap = gateHeaps.get(gate)
                    .get(parkingSlot.getType());
            gateHeap.removeIf(slotDistance -> slotDistance.getSlot()
                    .getId()
                    .equals(parkingSlot.getId()));
        }
    }

    /**
     * Frees a slot and re-adds to all gate heaps
     */
    private synchronized void releaseSlot(Ticket ticket) {
        ParkingSlot parkingSlot = ticket.getSlot();
        parkingSlot.setStatus(SlotStatus.FREE);
        parkingSlotRepository.save(parkingSlot);

        for (Gate gate : Gate.values()) {
            int distance = distanceMap.getOrDefault(gate.name() + "_" + parkingSlot.getSlotNumber(), Integer.MAX_VALUE);
            gateHeaps.get(gate)
                    .get(parkingSlot.getType())
                    .offer(new SlotDistance(parkingSlot, distance));
        }
    }
}
