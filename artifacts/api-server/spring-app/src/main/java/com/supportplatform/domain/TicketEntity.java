package com.supportplatform.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tickets")
public class TicketEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "customer_id", nullable = false)
  private UserEntity customer;
  @Column(nullable = false) private String subject;
  @Column(nullable = false, columnDefinition = "TEXT") private String description;
  @Column(nullable = false) private String category;
  @Column(nullable = false) private String priority;
  @Column(nullable = false) private String status;
  private String sentiment;
  @Column(name = "ai_summary") private String aiSummary;
  @Column(name = "ai_analyzed_at") private OffsetDateTime aiAnalyzedAt;
  @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
  @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;
  protected TicketEntity() {}
}