# Railway deployment — beginner path

This is the easiest route to get the portfolio backend online without learning Docker first.

## What you need

- A GitHub account
- A Railway account
- An OpenAI API key
- This project pushed to a GitHub repository

## 1. Put the project on GitHub

In VS Code:

1. Open the `ai-customer-support-platform` folder.
2. Open **Source Control**.
3. Initialize the repository.
4. Create a new GitHub repository named `ai-customer-support-platform`.
5. Commit and push the project.

Do not commit a real OpenAI key. Keep secrets only in Railway variables.

## 2. Create the Railway project

1. Sign in to Railway.
2. Create a **New Project**.
3. Choose **Deploy from GitHub repo**.
4. Select `ai-customer-support-platform`.
5. Let Railway start the first build.

The repository contains `railway.toml`, which tells Railway to use its Railpack builder and health check. The project intentionally has no Dockerfile at the repository root, so Railway can use its normal Java/Maven build flow.

## 3. Create PostgreSQL with pgvector

Do **not** add the ordinary PostgreSQL template for this RAG project. Railway's RAG documentation states that the standard PostgreSQL image does not include pgvector and recommends the pgvector template.

In the Railway project:

1. Click **+ New**.
2. Choose the PostgreSQL/pgvector template from Railway's template marketplace.
3. Wait until the database is running.

## 4. Connect the application to the database

Open the application service and add these variables.

| Variable | Value |
|---|---|
| `DB_URL` | JDBC PostgreSQL URL for the Railway database |
| `DB_USERNAME` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |

The easiest method is to use Railway's **Add Reference** option and reference the corresponding variables from the database service. The database service exposes PostgreSQL connection variables including `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE`, and `DATABASE_URL`.

Build the JDBC URL as:

```text
jdbc:postgresql://<PGHOST>:<PGPORT>/<PGDATABASE>
```

Do not put the actual password into the URL; keep it in `DB_PASSWORD`.

## 5. Add application secrets

Add:

```text
JWT_SECRET=<a long random string, at least 32 bytes>
OPENAI_API_KEY=<your OpenAI API key>
DEMO_DATA_ENABLED=true
```

Keep:

```text
OPENAI_CHAT_MODEL=gpt-4o-mini
OPENAI_EMBEDDING_MODEL=text-embedding-3-small
OPENAI_EMBEDDING_DIMENSIONS=1536
OPENAI_TOP_K=4
```

The first deployment creates the tables from `src/main/resources/schema.sql` and seeds demo users/knowledge when `DEMO_DATA_ENABLED=true`.

## 6. Deploy and check logs

Commit any changes and push them to GitHub. Railway should create a new deployment automatically.

Wait for the build to finish, then open the deployment logs. A healthy application should expose:

```text
GET /actuator/health
```

Expected response:

```json
{"status":"UP"}
```

## 7. Generate the public URL

Open the application service settings and go to **Networking**. Generate a public domain.

Your base URL will look similar to:

```text
https://<your-service>.up.railway.app
```

## 8. Test login

```http
POST https://<your-service>.up.railway.app/api/auth/login
Content-Type: application/json

{
  "email": "customer@example.com",
  "password": "Customer@12345"
}
```

Copy the returned `token`.

## 9. Create a ticket

```http
POST https://<your-service>.up.railway.app/api/tickets
Authorization: Bearer <TOKEN>
Content-Type: application/json

{
  "subject": "I was charged twice",
  "description": "I see two charges for the same order. Please help."
}
```

Save the returned ticket id.

## 10. Test AI categorization

```http
POST https://<your-service>.up.railway.app/api/tickets/<TICKET_ID>/ai/analyze
Authorization: Bearer <TOKEN>
```

Expected shape:

```json
{
  "category": "BILLING",
  "priority": "HIGH",
  "sentiment": "NEGATIVE",
  "summary": "..."
}
```

## 11. Test RAG response generation

```http
POST https://<your-service>.up.railway.app/api/tickets/<TICKET_ID>/ai/reply
Authorization: Bearer <TOKEN>
```

The response contains the drafted reply and retrieved knowledge sources.

## 12. Add your own knowledge document

Login as the demo agent:

```http
POST https://<your-service>.up.railway.app/api/auth/login
Content-Type: application/json

{
  "email": "agent@example.com",
  "password": "Agent@12345"
}
```

Then:

```http
POST https://<your-service>.up.railway.app/api/knowledge
Authorization: Bearer <AGENT_TOKEN>
Content-Type: application/json

{
  "title": "Cancellation policy",
  "content": "Customers may cancel an order before it enters fulfillment.",
  "source": "cancellation-policy.md"
}
```

The application creates the embedding and stores it in PostgreSQL/pgvector.

## 13. Portfolio demo order

For an interview demo, show this sequence:

```text
1. Health check
2. Customer login -> JWT
3. Create ticket
4. AI categorize ticket
5. Add knowledge article
6. AI draft reply
7. Show returned sources
8. Explain JWT + role-based access
9. Explain pgvector similarity search
10. Explain Railway deployment
```
