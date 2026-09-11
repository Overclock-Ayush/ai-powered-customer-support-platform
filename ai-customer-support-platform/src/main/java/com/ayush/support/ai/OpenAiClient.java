package com.ayush.support.ai;

import com.google.gson.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpenAiClient {
    private final String baseUrl;
    private final String apiKey;
    private final String chatModel;
    private final String embeddingModel;
    private final int embeddingDimensions;
    private final HttpClient httpClient;

    public OpenAiClient(
            @Value("${app.openai.base-url}") String baseUrl,
            @Value("${app.openai.api-key}") String apiKey,
            @Value("${app.openai.chat-model}") String chatModel,
            @Value("${app.openai.embedding-model}") String embeddingModel,
            @Value("${app.openai.embedding-dimensions}") int embeddingDimensions,
            @Value("${app.openai.timeout-seconds}") long timeoutSeconds) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
        this.embeddingDimensions = embeddingDimensions;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(timeoutSeconds)).build();
    }

    public List<Float> embed(String input) {
        requireApiKey();
        JsonObject body = new JsonObject();
        body.addProperty("model", embeddingModel);
        body.addProperty("input", input);
        if (embeddingDimensions > 0) body.addProperty("dimensions", embeddingDimensions);
        JsonObject json = post("/embeddings", body);
        JsonArray embedding = json.getAsJsonArray("data").get(0).getAsJsonObject().getAsJsonArray("embedding");
        List<Float> result = new ArrayList<>(embedding.size());
        for (JsonElement element : embedding) result.add(element.getAsFloat());
        return result;
    }

    public String chat(String systemPrompt, String userPrompt) {
        requireApiKey();
        JsonObject body = new JsonObject();
        body.addProperty("model", chatModel);
        body.addProperty("temperature", 0.2);
        JsonArray messages = new JsonArray();
        JsonObject system = new JsonObject();
        system.addProperty("role", "system");
        system.addProperty("content", systemPrompt);
        JsonObject user = new JsonObject();
        user.addProperty("role", "user");
        user.addProperty("content", userPrompt);
        messages.add(system);
        messages.add(user);
        body.add("messages", messages);
        JsonObject format = new JsonObject();
        format.addProperty("type", "json_object");
        body.add("response_format", format);

        JsonObject json = post("/chat/completions", body);
        return json.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();
    }

    private JsonObject post(String path, JsonObject body) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(120))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("OpenAI API request failed: HTTP " + response.statusCode() + " - " + truncate(response.body(), 400));
            }
            return JsonParser.parseString(response.body()).getAsJsonObject();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while calling LLM provider", e);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to reach LLM provider", e);
        }
    }

    private void requireApiKey() {
        if (apiKey.isBlank()) throw new IllegalStateException("OPENAI_API_KEY is not configured");
    }

    private String truncate(String value, int max) {
        if (value == null) return "";
        return value.length() <= max ? value : value.substring(0, max);
    }
}
