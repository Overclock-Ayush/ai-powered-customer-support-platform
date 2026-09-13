package com.supportplatform.api;

import com.supportplatform.ai.GeminiService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
  private final SupportRepository repository;
  private final GeminiService gemini;
  public TicketController(SupportRepository repository, GeminiService gemini) { this.repository = repository; this.gemini = gemini; }

  @GetMapping
  public List<ApiModels.Ticket> list(Authentication auth,
                                     @RequestParam(required=false) String status,
                                     @RequestParam(required=false) String priority,
                                     @RequestParam(required=false) String search) {
    return repository.tickets(userId(auth), role(auth), status, priority, search);
  }
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('CUSTOMER','AGENT','ADMIN')")
  public ApiModels.Ticket create(Authentication auth, @Valid @RequestBody ApiModels.TicketInput input) {
    return repository.createTicket(userId(auth), input);
  }
  @GetMapping("/{id}")
  public ApiModels.Ticket get(@PathVariable long id, Authentication auth) {
    ApiModels.Ticket ticket = repository.ticket(id);
    if ("CUSTOMER".equals(role(auth)) && ticket.customer().id() != userId(auth)) throw new org.springframework.security.access.AccessDeniedException("Not your ticket");
    return ticket;
  }
  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
  public ApiModels.Ticket update(@PathVariable long id, @RequestBody ApiModels.TicketUpdate input) { return repository.updateTicket(id, input); }
  @PatchMapping("/{id}/status")
  @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
  public ApiModels.Ticket status(@PathVariable long id, @Valid @RequestBody ApiModels.StatusUpdate input) { return repository.status(id, input.status()); }
  @PostMapping("/{id}/ai/analyze")
  @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
  public ApiModels.Ticket analyze(@PathVariable long id) {
    ApiModels.Ticket ticket = repository.ticket(id);
    var result = gemini.analyze(ticket);
    repository.analysis(id, result.path("category").asText("GENERAL"), result.path("priority").asText("MEDIUM"),
        result.path("sentiment").asText("NEUTRAL"), result.path("summary").asText());
    return repository.ticket(id);
  }
  @PostMapping("/{id}/ai/reply")
  @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
  public ApiModels.RagReply reply(@PathVariable long id) { return gemini.reply(repository.ticket(id)); }
  private long userId(Authentication auth) { return ((Number) auth.getDetails()).longValue(); }
  private String role(Authentication auth) { return auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""); }
}