package com.example.parking;

import com.example.parking.dto.EntryRequest;
import com.example.parking.dto.TicketResponse;
import com.example.parking.entity.VehicleType;
import com.example.parking.service.ParkingService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ParkingServiceIntegrationTest {

    @Autowired
    ParkingService parkingService;

    @Disabled
    @Test
    void testEntryAndExit() {
        EntryRequest req = new EntryRequest();
        req.setPlateNo("MH12AB1234");
        req.setType(VehicleType.CAR);
        req.setEntryGate("GATE_1");
        req.setOwnerName("Test Owner");

        TicketResponse resp = parkingService.enterVehicle(req);
        assert resp.getTicketId() != null;
    }
}
