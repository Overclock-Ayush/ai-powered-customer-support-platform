# Project verification checklist

Before publishing the repository, confirm:

- [ ] Java 21 is used by `pom.xml`.
- [ ] `railway.toml` is present at the repository root.
- [ ] There is no Dockerfile at the repository root.
- [ ] Docker files exist only under `deployment/docker/`.
- [ ] `OPENAI_API_KEY` is not committed anywhere.
- [ ] `src/main/resources/schema.sql` contains the pgvector extension and vector column.
- [ ] JWT authentication and role checks are enabled.
- [ ] AI analysis endpoint returns category, priority, sentiment and summary.
- [ ] RAG endpoint returns a reply plus source references.
