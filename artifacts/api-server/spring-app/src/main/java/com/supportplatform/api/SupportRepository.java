package com.supportplatform.api;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class SupportRepository {
  private final JdbcTemplate jdbc;
  public SupportRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  private final RowMapper<ApiModels.User> userMapper = (rs, n) ->
      new ApiModels.User(rs.getLong("id"), rs.getString("name"), rs.getString("email"), rs.getString("role"));

  public Map<String,Object> findUserByEmail(String email) {
    return jdbc.queryForMap("SELECT * FROM app_users WHERE lower(email)=lower(?)", email);
  }
  public ApiModels.User insertUser(String name, String email, String hash, String role) {
    return jdbc.queryForObject("INSERT INTO app_users(name,email,password_hash,role) VALUES (?,?,?,?) RETURNING id,name,email,role",
        userMapper, name, email, hash, role);
  }
  public ApiModels.User user(long id) {
    return jdbc.queryForObject("SELECT id,name,email,role FROM app_users WHERE id=?", userMapper, id);
  }
  private final RowMapper<ApiModels.Ticket> ticketMapper = (rs, n) -> {
    ApiModels.User user = new ApiModels.User(rs.getLong("customer_id"), rs.getString("customer_name"),
        rs.getString("customer_email"), rs.getString("customer_role"));
    return new ApiModels.Ticket(rs.getLong("id"), user, rs.getString("subject"), rs.getString("description"),
        rs.getString("category"), rs.getString("priority"), rs.getString("status"), rs.getString("sentiment"),
        rs.getString("ai_summary"), rs.getObject("ai_analyzed_at", OffsetDateTime.class),
        rs.getObject("created_at", OffsetDateTime.class), rs.getObject("updated_at", OffsetDateTime.class));
  };
  private String ticketSql(String where) {
    return "SELECT t.*, u.id customer_id,u.name customer_name,u.email customer_email,u.role customer_role FROM tickets t JOIN app_users u ON u.id=t.customer_id " + where + " ORDER BY t.updated_at DESC";
  }
  public List<ApiModels.Ticket> tickets(Long userId, String role, String status, String priority, String search) {
    StringBuilder where = new StringBuilder("WHERE 1=1");
    java.util.ArrayList<Object> args = new java.util.ArrayList<>();
    if ("CUSTOMER".equals(role)) { where.append(" AND t.customer_id=?"); args.add(userId); }
    if (status != null && !status.isBlank()) { where.append(" AND t.status=?"); args.add(status); }
    if (priority != null && !priority.isBlank()) { where.append(" AND t.priority=?"); args.add(priority); }
    if (search != null && !search.isBlank()) { where.append(" AND (t.subject ILIKE ? OR t.description ILIKE ?)"); args.add("%"+search+"%"); args.add("%"+search+"%"); }
    return jdbc.query(ticketSql(where.toString()), ticketMapper, args.toArray());
  }
  public ApiModels.Ticket ticket(long id) { return jdbc.queryForObject(ticketSql("WHERE t.id=?"), ticketMapper, id); }
  public ApiModels.Ticket createTicket(long userId, ApiModels.TicketInput input) {
    Long id = jdbc.queryForObject("INSERT INTO tickets(customer_id,subject,description,category,priority) VALUES (?,?,?,?,?) RETURNING id",
        Long.class, userId, input.subject(), input.description(), val(input.category(), "GENERAL"), val(input.priority(), "MEDIUM"));
    return ticket(id);
  }
  public ApiModels.Ticket updateTicket(long id, ApiModels.TicketUpdate input) {
    jdbc.update("UPDATE tickets SET subject=COALESCE(?,subject),description=COALESCE(?,description),category=COALESCE(?,category),priority=COALESCE(?,priority),updated_at=NOW() WHERE id=?",
        input.subject(), input.description(), input.category(), input.priority(), id);
    return ticket(id);
  }
  public ApiModels.Ticket status(long id, String status) {
    jdbc.update("UPDATE tickets SET status=?,updated_at=NOW() WHERE id=?", status, id);
    return ticket(id);
  }
  public void analysis(long id, String category, String priority, String sentiment, String summary) {
    jdbc.update("UPDATE tickets SET category=?,priority=?,sentiment=?,ai_summary=?,ai_analyzed_at=NOW(),updated_at=NOW() WHERE id=?",
        category, priority, sentiment, summary, id);
  }
  public List<Map<String,Object>> knowledgeRows(String search) {
    if (search == null || search.isBlank()) return jdbc.queryForList("SELECT * FROM knowledge_documents ORDER BY updated_at DESC");
    return jdbc.queryForList("SELECT * FROM knowledge_documents WHERE title ILIKE ? OR content ILIKE ? ORDER BY updated_at DESC", "%"+search+"%", "%"+search+"%");
  }
  public Map<String,Object> knowledge(long id) { return jdbc.queryForMap("SELECT * FROM knowledge_documents WHERE id=?", id); }
  public long createKnowledge(ApiModels.KnowledgeInput i) {
    return jdbc.queryForObject("INSERT INTO knowledge_documents(title,content,category) VALUES (?,?,?) RETURNING id", Long.class, i.title(), i.content(), i.category());
  }
  public void saveEmbedding(long id, float[] vector) {
    String literal = java.util.Arrays.toString(vector).replace(" ", "");
    jdbc.update("UPDATE knowledge_documents SET embedding=?::vector,updated_at=NOW() WHERE id=?", literal, id);
  }
  public List<Map<String,Object>> similar(float[] vector, int limit) {
    String literal = java.util.Arrays.toString(vector).replace(" ", "");
    return jdbc.queryForList("SELECT id,title,1-(embedding <=> ?::vector) similarity FROM knowledge_documents WHERE embedding IS NOT NULL ORDER BY embedding <=> ?::vector LIMIT ?",
        literal, literal, limit);
  }
  public ApiModels.Dashboard dashboard(Long userId, String role) {
    String scope = "CUSTOMER".equals(role) ? " WHERE customer_id="+userId : "";
    long total = jdbc.queryForObject("SELECT COUNT(*) FROM tickets"+scope, Long.class);
    long open = count(scope, "OPEN"), progress = count(scope, "IN_PROGRESS"), resolved = count(scope, "RESOLVED"), critical = count(scope, "CRITICAL");
    return new ApiModels.Dashboard(total, open, progress, resolved, critical, tickets(userId, role, null, null, null).stream().limit(5).toList());
  }
  private long count(String scope, String value) {
    String conjunction = scope.isBlank() ? " WHERE " : " AND ";
    return jdbc.queryForObject("SELECT COUNT(*) FROM tickets"+scope+conjunction+"status=?", Long.class, value);
  }
  private static String val(String value, String fallback) { return value == null || value.isBlank() ? fallback : value; }
}