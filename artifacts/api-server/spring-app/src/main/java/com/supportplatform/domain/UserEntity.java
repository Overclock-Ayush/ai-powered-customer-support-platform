package com.supportplatform.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "app_users")
public class UserEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false) private String name;
  @Column(nullable = false, unique = true) private String email;
  @Column(name = "password_hash", nullable = false) private String passwordHash;
  @Column(nullable = false) private String role;
  @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
  protected UserEntity() {}
}