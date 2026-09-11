package com.ayush.support.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public final class KnowledgeDtos {
    private KnowledgeDtos() {}

    public record CreateKnowledgeRequest(
            @NotBlank @Size(max = 200) String title,
            @NotBlank String content,
            @Size(max = 255) String source) {}

    public record KnowledgeResponse(
            UUID id,
            String title,
            String content,
            String source,
            Instant createdAt,
            Instant updatedAt) {}
}
