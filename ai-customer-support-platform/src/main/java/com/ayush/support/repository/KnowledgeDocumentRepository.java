package com.ayush.support.repository;

import com.ayush.support.domain.KnowledgeDocument;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, UUID> {
}
