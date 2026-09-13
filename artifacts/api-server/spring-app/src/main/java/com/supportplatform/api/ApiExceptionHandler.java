package com.supportplatform.api;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  Map<String,String> unauthorized(Exception e) { return Map.of("error", "Invalid email or password"); }
  @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  Map<String,String> forbidden(Exception e) { return Map.of("error", "You do not have permission for this action"); }
  @ExceptionHandler({IllegalArgumentException.class, jakarta.validation.ConstraintViolationException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  Map<String,String> badRequest(Exception e) { return Map.of("error", e.getMessage() == null ? "Invalid request" : e.getMessage()); }
  @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  Map<String,String> notFound(Exception e) { return Map.of("error", "Resource not found"); }
}