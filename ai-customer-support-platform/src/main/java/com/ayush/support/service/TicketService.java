package com.ayush.support.service;

import com.ayush.support.api.TicketDtos;
import com.ayush.support.domain.Role;
import com.ayush.support.domain.Ticket;
import com.ayush.support.domain.TicketPriority;
import com.ayush.support.domain.TicketStatus;
import com.ayush.support.domain.User;
import com.ayush.support.repository.TicketRepository;
import com.ayush.support.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository, CurrentUserService currentUserService) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public TicketDtos.TicketResponse create(TicketDtos.CreateTicketRequest request) {
        Ticket ticket = new Ticket();
        ticket.setSubject(request.subject().trim());
        ticket.setDescription(request.description().trim());
        ticket.setCustomer(currentUserService.get());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(TicketPriority.MEDIUM);
        return toResponse(ticketRepository.save(ticket));
    }

    @Transactional(readOnly = true)
    public List<TicketDtos.TicketResponse> list() {
        User current = currentUserService.get();
        List<Ticket> tickets = current.getRole() == Role.CUSTOMER
                ? ticketRepository.findByCustomerIdOrderByCreatedAtDesc(current.getId())
                : ticketRepository.findAllByOrderByCreatedAtDesc();
        return tickets.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TicketDtos.TicketResponse get(UUID id) {
        Ticket ticket = find(id);
        checkAccess(ticket);
        return toResponse(ticket);
    }

    @Transactional(readOnly = true)
    public Ticket getEntity(UUID id) {
        Ticket ticket = find(id);
        checkAccess(ticket);
        return ticket;
    }

    @Transactional
    public TicketDtos.TicketResponse update(UUID id, TicketDtos.UpdateTicketRequest request) {
        Ticket ticket = find(id);
        User current = currentUserService.get();
        if (current.getRole() == Role.CUSTOMER && !ticket.getCustomer().getId().equals(current.getId())) {
            throw new AccessDeniedException("You cannot modify this ticket");
        }
        if (request.status() != null) ticket.setStatus(request.status());
        if (request.priority() != null) ticket.setPriority(request.priority());
        if (request.category() != null) ticket.setCategory(request.category());
        if (request.assignedAgentId() != null) {
            if (current.getRole() == Role.CUSTOMER) throw new AccessDeniedException("Customers cannot assign tickets");
            User agent = userRepository.findById(request.assignedAgentId())
                    .orElseThrow(() -> new IllegalArgumentException("Assigned user not found"));
            if (agent.getRole() != Role.AGENT) throw new IllegalArgumentException("assignedAgentId must reference an AGENT");
            ticket.setAssignedAgent(agent);
        }
        return toResponse(ticketRepository.save(ticket));
    }

    private Ticket find(UUID id) {
        return ticketRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));
    }

    private void checkAccess(Ticket ticket) {
        User current = currentUserService.get();
        if (current.getRole() == Role.CUSTOMER && !ticket.getCustomer().getId().equals(current.getId())) {
            throw new AccessDeniedException("You cannot access this ticket");
        }
    }

    private TicketDtos.TicketResponse toResponse(Ticket t) {
        return new TicketDtos.TicketResponse(
                t.getId(), t.getSubject(), t.getDescription(), t.getCategory(), t.getPriority(), t.getStatus(),
                t.getCustomer().getId(), t.getCustomer().getFullName(),
                t.getAssignedAgent() == null ? null : t.getAssignedAgent().getId(),
                t.getAssignedAgent() == null ? null : t.getAssignedAgent().getFullName(),
                t.getCreatedAt(), t.getUpdatedAt());
    }
}
