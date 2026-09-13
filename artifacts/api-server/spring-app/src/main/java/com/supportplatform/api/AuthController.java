package com.supportplatform.api;

import com.supportplatform.security.JwtService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final SupportRepository repository;
  private final PasswordEncoder encoder;
  private final JwtService jwt;
  public AuthController(SupportRepository repository, PasswordEncoder encoder, JwtService jwt) {
    this.repository = repository; this.encoder = encoder; this.jwt = jwt;
  }
  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiModels.AuthResponse register(@Valid @RequestBody ApiModels.RegisterInput input) {
    try {
      ApiModels.User user = repository.insertUser(input.name(), input.email().toLowerCase(), encoder.encode(input.password()), "CUSTOMER");
      return new ApiModels.AuthResponse(jwt.create(user.id(), user.email(), user.role()), user);
    } catch (org.springframework.dao.DuplicateKeyException e) {
      throw new IllegalArgumentException("An account with that email already exists");
    }
  }
  @PostMapping("/login")
  public ApiModels.AuthResponse login(@Valid @RequestBody ApiModels.LoginInput input) {
    Map<String,Object> row;
    try { row = repository.findUserByEmail(input.email()); }
    catch (Exception e) { throw new org.springframework.security.authentication.BadCredentialsException("Invalid email or password"); }
    if (!encoder.matches(input.password(), String.valueOf(row.get("password_hash"))))
      throw new org.springframework.security.authentication.BadCredentialsException("Invalid email or password");
    ApiModels.User user = new ApiModels.User(((Number) row.get("id")).longValue(), String.valueOf(row.get("name")),
        String.valueOf(row.get("email")), String.valueOf(row.get("role")));
    return new ApiModels.AuthResponse(jwt.create(user.id(), user.email(), user.role()), user);
  }
}