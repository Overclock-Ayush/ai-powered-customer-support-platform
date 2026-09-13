package com.supportplatform.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "knowledge_documents")
public class KnowledgeDocumentEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false) private String title;
  @Column(nullable = false, columnDefinition = "TEXT") private String content;
  @Column(nullable = false) private String category;
  @Column(columnDefinition = "vector(768)") private String embedding;
  @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
  @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;
  protected KnowledgeDocumentEntity() {}
}