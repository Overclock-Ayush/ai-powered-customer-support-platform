package com.supportplatform.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supportplatform.api.ApiModels;
import com.supportplatform.api.SupportRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GeminiService {
  private final RestClient http;
  private final ObjectMapper mapper;
  private final SupportRepository repository;
  private final String apiKey;
  private final String chatModel;
  private final String embeddingModel;

  public GeminiService(ObjectMapper mapper, SupportRepository repository,
                       @Value("${gemini.base-url}") String baseUrl,
                       @Value("${gemini.api-key}") String apiKey,
                       @Value("${gemini.chat-model}") String chatModel,
                       @Value("${gemini.embedding-model}") String embeddingModel) {
    this.http = RestClient.builder().baseUrl(baseUrl).build();
    this.mapper = mapper;
    this.repository = repository;
    this.apiKey = apiKey;
    this.chatModel = chatModel;
    this.embeddingModel = embeddingModel;
  }

  public JsonNode analyze(ApiModels.Ticket ticket) {
    String prompt = """
        Analyze this customer support ticket. Return ONLY valid JSON with exactly these keys:
        category (one of BILLING, TECHNICAL, ACCOUNT, SHIPPING, REFUND, GENERAL),
        priority (one of LOW, MEDIUM, HIGH, CRITICAL),
        sentiment (one of POSITIVE, NEUTRAL, NEGATIVE, URGENT),
        summary (a concise sentence).
        Ticket subject: %s
        Ticket description: %s
        """.formatted(ticket.subject(), ticket.description());
    return generateJson(prompt);
  }

  public float[] embed(String text) {
    requireKey();
    Map<String,Object> body = Map.of(
        "content", Map.of("parts", List.of(Map.of("text", text))),
        "outputDimensionality", 768
    );
    JsonNode result = http.post().uri(uri -> uri.path("/models/{model}:embedContent")
            .queryParam("key", apiKey).build(embeddingModel))
        .contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(JsonNode.class);
    JsonNode values = result == null ? null : result.path("embedding").path("values");
    if (values == null || !values.isArray()) throw new IllegalStateException("Gemini did not return an embedding");
    float[] vector = new float[values.size()];
    for (int i = 0; i < values.size(); i++) vector[i] = (float) values.get(i).asDouble();
    return vector;
  }

  public ApiModels.RagReply reply(ApiModels.Ticket ticket) {
    float[] query = embed(ticket.subject() + "\n" + ticket.description());
    List<Map<String,Object>> rows = repository.similar(query, 4);
    List<ApiModels.RagSource> sources = rows.stream()
        .map(row -> new ApiModels.RagSource(((Number) row.get("id")).longValue(),
            String.valueOf(row.get("title")), ((Number) row.get("similarity")).doubleValue()))
        .toList();
    StringBuilder context = new StringBuilder();
    for (Map<String,Object> row : rows) {
      context.append("\nDOCUMENT: ").append(row.get("title")).append("\n");
      Map<String,Object> full = repository.knowledge(((Number) row.get("id")).longValue());
      context.append(full.get("content")).append("\n");
    }
    String prompt = """
        You are a customer support agent. Answer the ticket using only the provided knowledge base
        when it contains a relevant policy. Do not invent policies, refunds, timelines, or guarantees.
        If the knowledge base is not sufficient, say that the request needs human review.
        Be concise, helpful, and ready to send to the customer.
        Ticket subject: %s
        Ticket description: %s
        Knowledge base:
        %s
        """.formatted(ticket.subject(), ticket.description(), context);
    String answer = generateReplyText(prompt);
    return new ApiModels.RagReply(answer, sources);
  }

  private JsonNode generateJson(String prompt) {
    try { return mapper.readTree(generateText(prompt)); }
    catch (Exception e) { throw new IllegalStateException("Gemini returned invalid analysis JSON", e); }
  }

  private String generateText(String prompt) {
    return generateText(prompt, true);
  }

  private String generateReplyText(String prompt) {
    return generateText(prompt, false);
  }

  private String generateText(String prompt, boolean json) {
    requireKey();
    Map<String,Object> generationConfig = json
        ? Map.of("temperature", 0.2, "responseMimeType", "application/json")
        : Map.of("temperature", 0.3);
    Map<String,Object> body = Map.of(
        "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", prompt)))),
        "generationConfig", generationConfig
    );
    JsonNode result = http.post().uri(uri -> uri.path("/models/{model}:generateContent")
            .queryParam("key", apiKey).build(chatModel))
        .contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(JsonNode.class);
    JsonNode text = result == null ? null : result.path("candidates").path(0).path("content").path("parts").path(0).path("text");
    if (text == null || text.isMissingNode()) throw new IllegalStateException("Gemini did not return text");
    return text.asText();
  }

  private void requireKey() {
    if (apiKey == null || apiKey.isBlank()) throw new IllegalStateException("GEMINI_API_KEY is not configured");
  }
}