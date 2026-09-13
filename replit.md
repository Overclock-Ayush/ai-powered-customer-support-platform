# Northstar — AI Customer Support Platform

Northstar is a portfolio-ready customer support workspace for triaging tickets, using grounded Gemini intelligence, and keeping customer context in one place.

## Run & Operate

- `pnpm --filter @workspace/api-server run dev` — run the Spring Boot API (port 8080)
- `pnpm --filter @workspace/support-platform run dev` — run the React/Vite frontend
- `pnpm --filter @workspace/support-platform run typecheck` — typecheck the frontend
- `PORT=19574 BASE_PATH=/ pnpm --filter @workspace/support-platform run build` — production frontend build
- `./mvnw -f artifacts/api-server/spring-app/pom.xml compile -DskipTests` — compile the API
- `pnpm --filter @workspace/api-spec run codegen` — regenerate API hooks and Zod schemas from the OpenAPI spec
- Required env: `DATABASE_URL`, `SESSION_SECRET`, and `GEMINI_API_KEY`

## Stack

- pnpm workspaces, Node.js, TypeScript, React, and Vite
- API: Java 17+, Spring Boot 3.4, Spring Web, Spring Security, JWT, and Actuator
- DB: PostgreSQL 16, pgvector, JDBC template persistence, and JPA/Hibernate entity/repository mappings
- AI: direct Gemini API calls for `gemini-3.6-flash` chat and `gemini-embedding-001` 768-dimensional embeddings
- API contract: OpenAPI with generated React Query hooks and Zod schemas

## Where things live

- `artifacts/support-platform` — React workspace UI, routes, theme, and generated-client integration
- `artifacts/api-server/spring-app` — Spring Boot API, security, Gemini service, JDBC repository, JPA mappings, and seed data
- `lib/api-spec/openapi.yaml` — source-of-truth REST contract
- `artifacts/api-server/spring-app/src/main/resources/schema.sql` — PostgreSQL and pgvector schema
- `artifacts/api-server/spring-app/src/main/resources/application.yml` — runtime configuration and model defaults

## Architecture decisions

- JWT access tokens are stored in browser local storage for the demo workspace and attached by the generated API client.
- Customer users can access only their own tickets; agents and admins can operate across the queue.
- AI replies are generated only after semantic retrieval from the knowledge base and return their grounded source documents.
- Embeddings use 768 dimensions to match `gemini-embedding-001` and PostgreSQL performs cosine-distance retrieval with pgvector.

## Product

## Product

- Register and sign in as a customer, agent, or administrator.
- View queue metrics and recent conversations on the command center dashboard.
- Create, filter, inspect, and update support tickets.
- Run Gemini ticket analysis for category, priority, sentiment, and summary.
- Generate a suggested customer reply grounded in the knowledge base.
- Create and reindex knowledge documents, with similarity search backing AI replies.

## User preferences

No additional project-specific preferences have been provided.

## Gotchas

The frontend Vite config intentionally requires `PORT`; provide it for standalone build commands. The managed workflow supplies it automatically.

## Pointers

- See the `pnpm-workspace` skill for workspace structure, TypeScript setup, and package details.
