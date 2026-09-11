package com.ayush.support.controller;

import com.ayush.support.ai.RagService;
import com.ayush.support.api.AiDtos;
import com.ayush.support.api.TicketDtos;
import com.ayush.support.domain.Ticket;
import com.ayush.support.service.TicketService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService ticketService;
    private final RagService ragService;

    public TicketController(TicketService ticketService, RagService ragService) {
        this.ticketService = ticketService;
        this.ragService = ragService;
    }

    @GetMapping
    public List<TicketDtos.TicketResponse> list() { return ticketService.list(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketDtos.TicketResponse create(@Valid @RequestBody TicketDtos.CreateTicketRequest request) {
        return ticketService.create(request);
    }

    @GetMapping("/{id}")
    public TicketDtos.TicketResponse get(@PathVariable UUID id) { return ticketService.get(id); }

    @PatchMapping("/{id}")
    public TicketDtos.TicketResponse update(@PathVariable UUID id, @RequestBody TicketDtos.UpdateTicketRequest request) {
        return ticketService.update(id, request);
    }

    @PostMapping("/{id}/ai/analyze")
    public AiDtos.TicketAnalysis analyze(@PathVariable UUID id) {
        Ticket ticket = ticketService.getEntity(id);
        return ragService.analyze(ticket);
    }

    @PostMapping("/{id}/ai/reply")
    public AiDtos.AiReplyResponse reply(@PathVariable UUID id) {
        Ticket ticket = ticketService.getEntity(id);
        return ragService.draftReply(ticket);
    }
}
