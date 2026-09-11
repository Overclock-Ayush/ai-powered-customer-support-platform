# Interview talking points

## Why this stack?

- Java + Spring Boot for layered REST API development.
- Spring Security + JWT for stateless authentication and role-based authorization.
- PostgreSQL + JPA/Hibernate for transactional ticket and user data.
- pgvector for storing and searching embeddings directly inside PostgreSQL.
- Docker for reproducible local/production packaging.
- AWS ECS/Fargate + RDS as a practical deployment target.

## How categorization works

The ticket subject and description are sent to the LLM with a constrained JSON schema in the prompt. The service maps the returned category and priority to Java enums, with safe fallbacks.

## How RAG works

The ticket is embedded, the embedding is searched against the knowledge-base vectors using pgvector cosine distance, and the most relevant documents are inserted into the response prompt. The endpoint returns both the drafted reply and the retrieved sources.

## Error handling

Validation errors return HTTP 400. Invalid credentials return 401. Role/access violations return 403. Unexpected errors return 500 with a generic message.

## Production improvements

Add refresh tokens, secret rotation, database migrations (Flyway/Liquibase), structured audit logs, rate limiting, a real chunking pipeline, redaction of PII before model calls, async embedding jobs, observability, and automated CI/CD.
