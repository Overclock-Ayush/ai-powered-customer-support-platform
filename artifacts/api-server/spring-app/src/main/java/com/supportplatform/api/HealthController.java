package com.supportplatform.api;

import org.springframework.web.bind.annotation.*;

@RestController
public class HealthController {
  @GetMapping("/api/healthz")
  public java.util.Map<String,String> health() { return java.util.Map.of("status", "ok"); }
}