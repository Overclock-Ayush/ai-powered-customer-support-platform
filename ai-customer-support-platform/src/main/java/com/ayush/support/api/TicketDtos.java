package com.ayush.support.api;

import com.ayush.support.domain.TicketCategory;
import com.ayush.support.domain.TicketPriority;
import com.ayush.support.domain.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public final class TicketDtos {
    private TicketDtos() {}

    public record CreateTicketRequest(
            @NotBlank @Size(max = 200) String subject,
            @NotBlank String description) {}

    public record UpdateTicketRequest(
            TicketStatus status,
            TicketPriority priority,
            TicketCategory category,
            UUID assignedAgentId) {}

    public record TicketResponse(
            UUID id,
            String subject,
            String description,
            TicketCategory category,
            TicketPriority priority,
            TicketStatus status,
            UUID customerId,
            String customerName,
            UUID assignedAgentId,
            String assignedAgentName,
            Instant createdAt,
            Instant updatedAt) {}
}
