package com.ayush.support.ai;

import com.ayush.support.api.AiDtos;
import com.ayush.support.domain.Ticket;
import com.ayush.support.domain.TicketCategory;
import com.ayush.support.domain.TicketPriority;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RagService {
    private final OpenAiClient client;
    private final KnowledgeVectorRepository vectorRepository;
    private final int topK;

    public RagService(OpenAiClient client,
                      KnowledgeVectorRepository vectorRepository,
                      @Value("${app.openai.top-k}") int topK) {
        this.client = client;
        this.vectorRepository = vectorRepository;
        this.topK = Math.max(1, topK);
    }

    public AiDtos.TicketAnalysis analyze(Ticket ticket) {
        String input = "Subject: " + ticket.getSubject() + "\nDescription: " + ticket.getDescription();
        String prompt = """
                Analyze this customer support ticket. Return JSON only with exactly these keys:
                category (one of BILLING, TECHNICAL, ACCOUNT, SHIPPING, REFUND, GENERAL),
                priority (one of LOW, MEDIUM, HIGH, URGENT),
                sentiment (one of POSITIVE, NEUTRAL, NEGATIVE),
                summary (one sentence).
                Do not invent facts.
                """;
        String content = client.chat(prompt, input);
        JsonObject json = JsonParser.parseString(stripCodeFence(content)).getAsJsonObject();
        TicketCategory category = parseCategory(json, TicketCategory.GENERAL);
        TicketPriority priority = parsePriority(json, TicketPriority.MEDIUM);
        String sentiment = getString(json, "sentiment", "NEUTRAL");
        String summary = getString(json, "summary", "No summary returned.");
        return new AiDtos.TicketAnalysis(category, priority, sentiment, summary);
    }

    public AiDtos.AiReplyResponse draftReply(Ticket ticket) {
        String query = ticket.getSubject() + "\n" + ticket.getDescription();
        var embedding = client.embed(query);
        var results = vectorRepository.search(VectorMath.toPgVector(embedding), topK);

        String context = results.stream()
                .map(d -> "TITLE: " + d.title()
                        + "\nSOURCE: " + (d.source() == null ? "internal" : d.source())
                        + "\nCONTENT: " + d.content())
                .collect(Collectors.joining("\n\n---\n\n"));

        if (context.isBlank()) {
            context = "No knowledge-base article matched this request.";
        }

        String system = """
                You are a customer support agent. Draft a concise, helpful response.
                Use only the supplied knowledge base when making factual product or policy claims.
                If the knowledge base is insufficient, clearly say that an agent should verify the detail.
                Return JSON with exactly one key: reply.
                """;

        String user = """
                Ticket subject: %s
                Ticket description: %s

                Retrieved knowledge:
                %s
                """.formatted(ticket.getSubject(), ticket.getDescription(), context);

        String content = client.chat(system, user);
        JsonObject json = JsonParser.parseString(stripCodeFence(content)).getAsJsonObject();
        String reply = getString(json, "reply",
                "Please allow our support team to review this request.");

        List<AiDtos.SourceReference> sources = results.stream()
                .map(r -> new AiDtos.SourceReference(r.title(), r.source(), r.similarity()))
                .toList();

        return new AiDtos.AiReplyResponse(reply, sources);
    }

    private TicketCategory parseCategory(JsonObject json, TicketCategory fallback) {
        try {
            return TicketCategory.valueOf(getString(json, "category", fallback.name()).trim().toUpperCase());
        } catch (Exception e) {
            return fallback;
        }
    }

    private TicketPriority parsePriority(JsonObject json, TicketPriority fallback) {
        try {
            return TicketPriority.valueOf(getString(json, "priority", fallback.name()).trim().toUpperCase());
        } catch (Exception e) {
            return fallback;
        }
    }

    private String getString(JsonObject json, String key, String fallback) {
        try {
            if (json.has(key) && !json.get(key).isJsonNull()) {
                return json.get(key).getAsString();
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private String stripCodeFence(String value) {
        return value == null ? "{}" : value.replace("```json", "").replace("```", "").trim();
    }
}
