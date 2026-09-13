package com.supportplatform.api;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
  private final SupportRepository repository;
  public DashboardController(SupportRepository repository) { this.repository = repository; }
  @GetMapping("/summary")
  public ApiModels.Dashboard summary(Authentication auth) {
    long id = ((Number) auth.getDetails()).longValue();
    String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
    return repository.dashboard(id, role);
  }
}