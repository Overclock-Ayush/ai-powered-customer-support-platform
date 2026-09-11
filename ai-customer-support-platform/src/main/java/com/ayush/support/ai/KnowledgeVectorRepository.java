package com.ayush.support.ai;

import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class KnowledgeVectorRepository {
    public record SimilarDocument(UUID id, String title, String content, String source, double similarity) {}

    private final JdbcTemplate jdbcTemplate;

    public KnowledgeVectorRepository(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public void upsert(UUID documentId, String vectorLiteral) {
        String sql = """
                INSERT INTO knowledge_embeddings(document_id, embedding, updated_at)
                VALUES (?, CAST(? AS vector), CURRENT_TIMESTAMP)
                ON CONFLICT (document_id)
                DO UPDATE SET embedding = EXCLUDED.embedding, updated_at = CURRENT_TIMESTAMP
                """;
        jdbcTemplate.update(sql, documentId, vectorLiteral);
    }

    public List<SimilarDocument> search(String vectorLiteral, int limit) {
        String sql = """
                SELECT kd.id, kd.title, kd.content, kd.source,
                       1 - (ke.embedding <=> CAST(? AS vector)) AS similarity
                FROM knowledge_embeddings ke
                JOIN knowledge_documents kd ON kd.id = ke.document_id
                ORDER BY ke.embedding <=> CAST(? AS vector)
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, ps -> {
            ps.setString(1, vectorLiteral);
            ps.setString(2, vectorLiteral);
            ps.setInt(3, limit);
        }, (rs, rowNum) -> new SimilarDocument(
                UUID.fromString(rs.getString("id")),
                rs.getString("title"),
                rs.getString("content"),
                rs.getString("source"),
                rs.getDouble("similarity")));
    }
}
