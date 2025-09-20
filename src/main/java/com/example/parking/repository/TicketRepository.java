package com.example.parking.repository;

import com.example.parking.entity.Ticket;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @Query("select t from Ticket t where t.vehicle.plateNo = ?1 and t.status = 'ACTIVE'")
    Optional<Ticket> findActiveTicketByPlate(String plateNo);
}
