package com.supportplatform.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {
  @GetMapping({"/", "/login", "/register", "/tickets", "/tickets/new", "/knowledge", "/settings", "/dashboard"})
  public String spa() {
    return "forward:/index.html";
  }

  @GetMapping("/tickets/{id}")
  public String ticket() {
    return "forward:/index.html";
  }
}
