package com.ayush.support.controller;

import com.ayush.support.api.KnowledgeDtos;
import com.ayush.support.service.KnowledgeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {
    private final KnowledgeService service;
    public KnowledgeController(KnowledgeService service) { this.service = service; }

    @GetMapping
    public List<KnowledgeDtos.KnowledgeResponse> list() { return service.list(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public KnowledgeDtos.KnowledgeResponse create(@Valid @RequestBody KnowledgeDtos.CreateKnowledgeRequest request) {
        return service.create(request);
    }

    @PostMapping("/reindex")
    public String reindex() { return service.reindexAll(); }
}
