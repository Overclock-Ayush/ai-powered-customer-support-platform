package com.supportplatform.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;

public final class ApiModels {
  private ApiModels() {}
  public record User(long id, String name, String email, String role) {}
  public record AuthResponse(String token, User user) {}
  public record RegisterInput(@NotBlank @Size(min = 2) String name, @NotBlank @Email String email,
                              @NotBlank @Size(min = 8) String password) {}
  public record LoginInput(@NotBlank @Email String email, @NotBlank String password) {}
  public record TicketInput(@NotBlank @Size(min = 3) String subject, @NotBlank @Size(min = 10) String description,
                            String category, String priority) {}
  public record TicketUpdate(String subject, String description, String category, String priority) {}
  public record StatusUpdate(@NotBlank String status) {}
  public record Ticket(long id, User customer, String subject, String description, String category,
                       String priority, String status, String sentiment, String aiSummary,
                       OffsetDateTime aiAnalyzedAt, OffsetDateTime createdAt, OffsetDateTime updatedAt) {}
  public record KnowledgeInput(@NotBlank @Size(min = 3) String title, @NotBlank @Size(min = 20) String content,
                               @NotBlank String category) {}
  public record Knowledge(long id, String title, String content, String category, boolean hasEmbedding,
                          OffsetDateTime createdAt, OffsetDateTime updatedAt) {}
  public record RagSource(long documentId, String title, double similarity) {}
  public record RagReply(String answer, List<RagSource> sources) {}
  public record Dashboard(long total, long open, long inProgress, long resolved, long critical, List<Ticket> recent) {}
}