package com.ayush.support.repository;

import com.ayush.support.domain.Ticket;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    List<Ticket> findAllByOrderByCreatedAtDesc();
}
