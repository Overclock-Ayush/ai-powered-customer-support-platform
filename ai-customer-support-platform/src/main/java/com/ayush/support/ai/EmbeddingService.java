package com.ayush.support.ai;

import com.ayush.support.domain.KnowledgeDocument;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingService {
    private final OpenAiClient openAiClient;
    private final KnowledgeVectorRepository vectorRepository;

    public EmbeddingService(OpenAiClient openAiClient, KnowledgeVectorRepository vectorRepository) {
        this.openAiClient = openAiClient;
        this.vectorRepository = vectorRepository;
    }

    public void index(KnowledgeDocument document) {
        String text = document.getTitle() + "\n" + document.getContent();
        var embedding = openAiClient.embed(text);
        vectorRepository.upsert(document.getId(), VectorMath.toPgVector(embedding));
    }
}
