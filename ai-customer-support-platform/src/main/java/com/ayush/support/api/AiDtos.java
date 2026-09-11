package com.ayush.support.api;

import com.ayush.support.domain.TicketCategory;
import com.ayush.support.domain.TicketPriority;
import java.util.List;

public final class AiDtos {
    private AiDtos() {}

    public record TicketAnalysis(
            TicketCategory category,
            TicketPriority priority,
            String sentiment,
            String summary) {}

    public record SourceReference(
            String title,
            String source,
            double similarity) {}

    public record AiReplyResponse(
            String reply,
            List<SourceReference> sources) {}
}
