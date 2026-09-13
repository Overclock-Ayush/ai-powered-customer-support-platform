package com.supportplatform.api;

import com.supportplatform.ai.GeminiService;
import jakarta.validation.Valid;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {
  private final SupportRepository repository;
  private final GeminiService gemini;
  public KnowledgeController(SupportRepository repository, GeminiService gemini) { this.repository = repository; this.gemini = gemini; }
  @GetMapping
  public List<ApiModels.Knowledge> list(@RequestParam(required=false) String search) {
    return repository.knowledgeRows(search).stream().map(this::map).toList();
  }
  @GetMapping("/{id}")
  public ApiModels.Knowledge get(@PathVariable long id) { return map(repository.knowledge(id)); }
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
  public ApiModels.Knowledge create(@Valid @RequestBody ApiModels.KnowledgeInput input) {
    long id = repository.createKnowledge(input);
    return map(repository.knowledge(id));
  }
  @PostMapping("/reindex")
  @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
  public Map<String,Integer> reindex() {
    List<Map<String,Object>> rows = repository.knowledgeRows(null);
    int succeeded = 0;
    for (Map<String,Object> row : rows) {
      try { repository.saveEmbedding(((Number) row.get("id")).longValue(), gemini.embed(row.get("title") + "\n" + row.get("content"))); succeeded++; }
      catch (RuntimeException ignored) {}
    }
    return Map.of("processed", rows.size(), "succeeded", succeeded);
  }
  private ApiModels.Knowledge map(Map<String,Object> row) {
    Object created = row.get("created_at"), updated = row.get("updated_at");
    return new ApiModels.Knowledge(((Number) row.get("id")).longValue(), String.valueOf(row.get("title")),
        String.valueOf(row.get("content")), String.valueOf(row.get("category")), row.get("embedding") != null,
        asTime(created), asTime(updated));
  }
  private OffsetDateTime asTime(Object value) {
    if (value instanceof OffsetDateTime time) return time;
    if (value instanceof Timestamp timestamp) return timestamp.toInstant().atOffset(java.time.ZoneOffset.UTC);
    return OffsetDateTime.now();
  }
}