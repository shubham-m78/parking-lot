package com.example.parking.repository;

import com.example.parking.entity.ParkingSlot;
import com.example.parking.entity.VehicleType;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

@Repository
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {

/*    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ParkingSlot s where s.type = :type and s.status = 'FREE' order by s.floorNumber, s.slotNumber")
    List<ParkingSlot> findAndLockFirstFreeSlotByType(VehicleType type);*/

    Optional<ParkingSlot> findBySlotNumber(String slotNumber);
}
