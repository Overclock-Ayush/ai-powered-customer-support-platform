package com.ayush.support.service;

import com.ayush.support.api.KnowledgeDtos;
import com.ayush.support.domain.KnowledgeDocument;
import com.ayush.support.repository.KnowledgeDocumentRepository;
import com.ayush.support.ai.EmbeddingService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KnowledgeService {
    private final KnowledgeDocumentRepository repository;
    private final EmbeddingService embeddingService;

    public KnowledgeService(KnowledgeDocumentRepository repository, EmbeddingService embeddingService) {
        this.repository = repository;
        this.embeddingService = embeddingService;
    }

    @Transactional
    public KnowledgeDtos.KnowledgeResponse create(KnowledgeDtos.CreateKnowledgeRequest request) {
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setTitle(request.title().trim());
        doc.setContent(request.content().trim());
        doc.setSource(request.source());
        doc = repository.save(doc);
        embeddingService.index(doc);
        return toResponse(doc);
    }

    @Transactional(readOnly = true)
    public List<KnowledgeDtos.KnowledgeResponse> list() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public String reindexAll() {
        List<KnowledgeDocument> docs = repository.findAll();
        docs.forEach(embeddingService::index);
        return "Indexed " + docs.size() + " knowledge documents";
    }

    private KnowledgeDtos.KnowledgeResponse toResponse(KnowledgeDocument d) {
        return new KnowledgeDtos.KnowledgeResponse(d.getId(), d.getTitle(), d.getContent(), d.getSource(), d.getCreatedAt(), d.getUpdatedAt());
    }
}
