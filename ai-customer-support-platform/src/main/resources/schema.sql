CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS app_users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    role VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS support_tickets (
    id UUID PRIMARY KEY,
    subject VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(40),
    priority VARCHAR(30),
    status VARCHAR(30) NOT NULL,
    customer_id UUID NOT NULL REFERENCES app_users(id),
    assigned_agent_id UUID REFERENCES app_users(id),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS knowledge_documents (
    id UUID PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    source VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS knowledge_embeddings (
    document_id UUID PRIMARY KEY REFERENCES knowledge_documents(id) ON DELETE CASCADE,
    embedding vector(1536) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_support_tickets_customer ON support_tickets(customer_id);
CREATE INDEX IF NOT EXISTS idx_support_tickets_status ON support_tickets(status);
CREATE INDEX IF NOT EXISTS idx_support_tickets_priority ON support_tickets(priority);
CREATE INDEX IF NOT EXISTS idx_knowledge_embeddings_hnsw
    ON knowledge_embeddings USING hnsw (embedding vector_cosine_ops);
