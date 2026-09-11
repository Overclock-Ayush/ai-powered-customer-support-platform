# Step-by-step: simplest way to get the project live

This guide uses **VS Code + GitHub + Railway**. You do not need to install Maven, PostgreSQL, pgvector, or Docker locally for the first deployment.

## Step 1 — Open the project in VS Code

Open the `ai-customer-support-platform` folder.

You already have Java 21 installed, so leave that setup as-is.

## Step 2 — Create a GitHub repository

In VS Code:

1. Open **Source Control**.
2. Initialize the repository if VS Code asks.
3. Sign in to GitHub from VS Code.
4. Choose **Publish to GitHub**.
5. Use the repository name `ai-customer-support-platform`.
6. Keep the repository public for an easy portfolio demo, unless you prefer private.

Before publishing, make sure `.env` is not committed. The project `.gitignore` already ignores `.env`.

## Step 3 — Create the Railway application service

1. Open Railway.
2. Create a **New Project**.
3. Choose **Deploy from GitHub repo**.
4. Select `ai-customer-support-platform`.
5. Start the deployment.

Railway's Java build system can detect a Maven/Spring Boot project and build it without a local Maven installation.

## Step 4 — Add PostgreSQL with pgvector

In the same Railway project:

1. Click **+ New**.
2. Open the template marketplace.
3. Deploy the **PostgreSQL + pgvector** option.
4. Wait for the database service to become healthy.

Do not use the ordinary PostgreSQL service for the RAG portion. The vector extension is required by the application schema.

## Step 5 — Connect the app to PostgreSQL

Open the application service's **Variables** section.

Add:

```text
DB_URL=jdbc:postgresql://<PGHOST>:<PGPORT>/<PGDATABASE>
DB_USERNAME=<PGUSER>
DB_PASSWORD=<PGPASSWORD>
```

Use Railway's variable **references** to the PostgreSQL service rather than manually copying a password where possible.

The Postgres service exposes connection variables such as `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, and `PGDATABASE`.

## Step 6 — Add the AI and security variables

Add:

```text
OPENAI_API_KEY=<your OpenAI API key>
JWT_SECRET=<a long random secret of at least 32 bytes>
DEMO_DATA_ENABLED=true
```

Leave these defaults unless you have a reason to change them:

```text
OPENAI_CHAT_MODEL=gpt-4o-mini
OPENAI_EMBEDDING_MODEL=text-embedding-3-small
OPENAI_EMBEDDING_DIMENSIONS=1536
OPENAI_TOP_K=4
OPENAI_TIMEOUT_SECONDS=60
```

Never commit the real API key to GitHub.

## Step 7 — Wait for the deployment

Railway should build and start the application after the variables are saved.

Open the deployment logs. If the service starts correctly, the application exposes:

```text
/actuator/health
```

## Step 8 — Generate a public URL

Open the application service:

**Settings → Networking → Generate Domain**

You will get a URL similar to:

```text
https://your-service-name.up.railway.app
```

## Step 9 — Test the health endpoint

Open:

```text
https://your-service-name.up.railway.app/actuator/health
```

Expected:

```json
{"status":"UP"}
```

## Step 10 — Import the Postman collection

Open Postman and import:

```text
postman/customer-support.postman_collection.json
```

Set the collection variable `baseUrl` to your Railway URL.

## Step 11 — Login

Run **Login Demo Customer**.

The response contains a JWT token. Copy it into the collection variable `token`.

## Step 12 — Create a ticket

Run **Create Ticket**.

Use:

```json
{
  "subject": "I was charged twice",
  "description": "I see two charges for the same order. Please help."
}
```

Copy the returned ticket id into the collection variable `ticketId`.

## Step 13 — Run AI categorization

Run **AI Analyze Ticket**.

The service asks the LLM to classify:

```text
category
priority
sentiment
summary
```

## Step 14 — Run the RAG response generator

Run **AI Draft Reply (RAG)**.

The backend performs:

```text
ticket text
   -> embedding API
   -> pgvector cosine similarity search
   -> relevant knowledge documents
   -> grounded LLM prompt
   -> response + sources
```

## Step 15 — Demonstrate role-based access

Login as:

```text
agent@example.com / Agent@12345
```

and show that knowledge-base APIs are available to an agent.

Then login as the customer and show that the customer cannot access agent/admin-only knowledge endpoints.

## Step 16 — Show the project in an interview

Use this explanation:

> I built a layered Spring Boot customer support backend with JWT authentication and role-based authorization. Ticket and user data are persisted in PostgreSQL using JPA/Hibernate. For AI, knowledge-base documents are converted into embeddings and stored in PostgreSQL with pgvector. For each ticket, the system retrieves relevant knowledge using cosine similarity and passes that context to the LLM to generate a grounded support response. I deployed the application from GitHub to Railway and kept Docker/AWS assets available for container and cloud deployment discussions.

## Troubleshooting

### Build fails

Open the Railway deployment logs and copy the first red error block. Do not change random files before checking the first error.

### Database connection fails

Verify the three variables:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

Also verify that the PostgreSQL + pgvector service is running.

### AI endpoints fail

Verify `OPENAI_API_KEY` is set in the Railway application service and that the API key is valid.

### RAG returns no sources

The knowledge rows may exist but have no embeddings. Log in as an agent/admin and run:

```text
POST /api/knowledge/reindex
```

Then call the RAG endpoint again.
