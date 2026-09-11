# Architecture

```text
Client / Postman
       |
       v
Spring MVC REST API
       |
       +--> Spring Security --> JWT Filter --> Role-based access
       |
       +--> Ticket Service --> JPA/Hibernate --> PostgreSQL
       |
       +--> Knowledge Service --> JPA/Hibernate --> PostgreSQL
       |
       +--> RAG Service
              |
              +--> Embedding API --> knowledge_embeddings (pgvector)
              |
              +--> cosine vector search --> top-k knowledge chunks
              |
              +--> Chat API + retrieved context --> grounded reply
```

## RAG flow

1. Store knowledge-base articles in PostgreSQL.
2. Convert each article into an embedding vector.
3. Save the vector in pgvector.
4. Convert an incoming ticket into a query embedding.
5. Run cosine similarity search against the vector index.
6. Inject the top-k matches into the LLM prompt.
7. Return a draft reply with source references.
