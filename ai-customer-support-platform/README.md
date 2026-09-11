# AI-Powered Customer Support Platform

A portfolio-ready Java backend that demonstrates the same core stack shown in the project description:

- Java 21 + Spring Boot
- REST APIs
- PostgreSQL + JPA/Hibernate
- Spring Security + JWT + role-based authorization
- LLM-powered ticket categorization and response generation
- RAG using embeddings + PostgreSQL pgvector cosine similarity search
- Docker assets for optional local/container deployment
- Railway deployment configuration for a beginner-friendly cloud path
- AWS ECS/RDS deployment notes for portfolio discussion

## The beginner-friendly deployment path

You do **not** need Docker or a local PostgreSQL installation to deploy this version to Railway.

The recommended workflow is:

```text
VS Code
  -> GitHub
  -> Railway API service
  -> Railway PostgreSQL + pgvector service
  -> OpenAI API
```

Railway can build a Spring Boot project from GitHub using its Railpack builder, and Railway's official RAG guide recommends a PostgreSQL service with pgvector for vector search. See `docs/railway-deployment.md` for the exact click-by-click process.

## Project structure

```text
src/main/java/com/ayush/support
├── ai                # OpenAI client, embeddings, RAG, pgvector search
├── api               # request/response DTOs
├── config            # demo-data seed
├── controller        # REST controllers
├── domain            # entities + enums
├── exception         # centralized API error handling
├── repository        # JPA repositories + vector repository
├── security          # JWT + Spring Security configuration
└── service           # business logic
```

## Main API flow

```text
POST /api/auth/login
        |
        v
JWT authentication
        |
        v
POST /api/tickets
        |
        +--> PostgreSQL
        |
        +--> POST /api/tickets/{id}/ai/analyze
        |       |
        |       +--> LLM categorization
        |
        +--> POST /api/tickets/{id}/ai/reply
                |
                +--> ticket embedding
                +--> pgvector similarity search
                +--> retrieved knowledge
                +--> LLM response generation
                +--> response + sources
```

## Demo accounts

When `DEMO_DATA_ENABLED=true`, the app seeds these accounts:

| Role | Email | Password |
|---|---|---|
| ADMIN | admin@example.com | Admin@12345 |
| AGENT | agent@example.com | Agent@12345 |
| CUSTOMER | customer@example.com | Customer@12345 |

These are for portfolio demos only. Change them before any real deployment.

## Useful API endpoints

```text
POST /api/auth/register
POST /api/auth/login

GET  /api/tickets
POST /api/tickets
GET  /api/tickets/{id}
PATCH /api/tickets/{id}

POST /api/tickets/{id}/ai/analyze
POST /api/tickets/{id}/ai/reply

GET  /api/knowledge
POST /api/knowledge
POST /api/knowledge/reindex

GET  /actuator/health
```

## Environment variables

### Railway deployment

Configure these in the Railway application service:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
OPENAI_API_KEY
DEMO_DATA_ENABLED
```

The AI model settings already have sensible defaults, but can also be overridden with:

```text
OPENAI_CHAT_MODEL
OPENAI_EMBEDDING_MODEL
OPENAI_EMBEDDING_DIMENSIONS
OPENAI_TOP_K
OPENAI_TIMEOUT_SECONDS
```


## Optional local Docker path

Docker files are intentionally under `deployment/docker/` so Railway does not automatically pick up a root Dockerfile. They are available when you later want to learn local/container deployment.

From that directory:

```bash
docker compose up --build
```

## AWS path

See `deployment/aws/README.md` for the ECS/Fargate + RDS architecture. This is optional for the first deployment; Railway is the recommended beginner path for getting the portfolio project live quickly.

## Important production upgrades

For a production system, add Flyway/Liquibase migrations, refresh-token rotation, rate limiting, PII controls, audit logging, async document ingestion, richer automated tests, and chunk-level embeddings.
