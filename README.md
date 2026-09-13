# Northstar

## AI Customer Support Platform

Northstar is a full-stack support operations workspace that helps teams move from an incoming customer message to a grounded, human-ready response. It combines ticket triage, role-based access, a searchable knowledge base, and Gemini-powered analysis and reply drafting.

### Features

- JWT authentication with BCrypt password hashing
- Customer, agent, and admin roles with ticket ownership rules
- Dashboard queue metrics and recent conversation view
- Ticket creation, filtering, status updates, and detail pages
- Gemini analysis for category, priority, sentiment, and concise summaries
- Gemini embeddings stored in PostgreSQL with pgvector cosine similarity search
- Grounded AI reply drafts with retrieved knowledge sources shown to the operator
- Knowledge document management and agent/admin reindexing
- Generated OpenAPI React Query client shared between frontend and API contract

### Demo accounts

| Role | Email | Password |
| --- | --- | --- |
| Customer | `customer@example.com` | `Customer@12345` |
| Agent | `agent@example.com` | `Agent@12345` |
| Admin | `admin@example.com` | `Admin@12345` |

### Stack

- **Frontend:** React, TypeScript, Vite, Tailwind CSS, TanStack Query, Wouter
- **Backend:** Java 17+, Spring Boot 3.4, Spring Web, Spring Security, JWT, Actuator
- **Persistence:** PostgreSQL, JDBC template, JPA/Hibernate, Spring Data repositories
- **Retrieval:** pgvector with 768-dimensional Gemini embeddings
- **AI:** Gemini `gemini-3.6-flash` and `gemini-embedding-001`

### Local development

The Replit workflows start both services:

```bash
pnpm --filter @workspace/support-platform run dev
./mvnw -f artifacts/api-server/spring-app/pom.xml spring-boot:run
```

The frontend is served through the artifact preview and calls the API through the routed `/api` path. The API listens on port `8080`.

Required environment variables:

- `DATABASE_URL`
- `SESSION_SECRET`
- `GEMINI_API_KEY`

### Verification

```bash
pnpm --filter @workspace/support-platform run typecheck
PORT=19574 BASE_PATH=/ pnpm --filter @workspace/support-platform run build
./mvnw -f artifacts/api-server/spring-app/pom.xml compile -DskipTests
```

Health endpoints:

- `GET /api/healthz`
- `GET /actuator/health`

### Project map

- `artifacts/support-platform` — web application and UI
- `artifacts/api-server/spring-app` — Spring Boot API
- `lib/api-spec/openapi.yaml` — REST contract
- `artifacts/api-server/spring-app/src/main/resources/schema.sql` — database schema