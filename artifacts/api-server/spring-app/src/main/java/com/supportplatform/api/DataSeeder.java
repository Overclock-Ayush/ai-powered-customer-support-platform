package com.supportplatform.api;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;

@Component
public class DataSeeder implements CommandLineRunner {
  private final JdbcTemplate jdbc;
  private final PasswordEncoder encoder;
  public DataSeeder(JdbcTemplate jdbc, PasswordEncoder encoder) { this.jdbc = jdbc; this.encoder = encoder; }
  @Override public void run(String... args) {
    Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM app_users", Integer.class);
    if (count != null && count == 0) {
      jdbc.update("INSERT INTO app_users(name,email,password_hash,role) VALUES (?,?,?,'CUSTOMER'),(?,?,?,'AGENT'),(?,?,?,'ADMIN')",
          "Demo Customer","customer@example.com",encoder.encode("Customer@12345"),
          "Support Agent","agent@example.com",encoder.encode("Agent@12345"),
          "Platform Admin","admin@example.com",encoder.encode("Admin@12345"));
      Long customer = jdbc.queryForObject("SELECT id FROM app_users WHERE email='customer@example.com'", Long.class);
      jdbc.update("INSERT INTO tickets(customer_id,subject,description,category,priority,status,sentiment,ai_summary) VALUES (?,?,?,?,?,?,?,?)",
          customer,"Duplicate payment on my card","I was charged twice for the same order and need help understanding the duplicate charge.",
          "BILLING","HIGH","OPEN","URGENT","Customer reports a likely duplicate charge and needs billing review.");
      jdbc.update("INSERT INTO tickets(customer_id,subject,description,category,priority,status) VALUES (?,?,?,?,?,?)",
          customer,"Where is my shipment?","My tracking has not updated for five days. Can you check the latest delivery status?",
          "SHIPPING","MEDIUM","IN_PROGRESS");
    }
    Integer docs = jdbc.queryForObject("SELECT COUNT(*) FROM knowledge_documents", Integer.class);
    if (docs != null && docs == 0) {
      add("Refund policy","REFUND","Customers may request a refund within 30 days of purchase. Approved refunds are returned to the original payment method within 5 to 10 business days.");
      add("Duplicate payment policy","BILLING","When a customer sees two charges for one order, verify whether one is a temporary authorization. If both charges settle, escalate to Billing for a reversal.");
      add("Password reset","ACCOUNT","Customers can reset a password from the sign-in page using Forgot password. The reset link expires after 30 minutes. Support should never ask for a password.");
      add("Account lockout","ACCOUNT","After five unsuccessful sign-in attempts, an account is locked for 15 minutes. An agent may verify the customer and request an unlock.");
      add("Failed payment","BILLING","For a failed payment, ask the customer to verify card expiry, billing address, and available funds before trying another payment method.");
      add("Order cancellation","SHIPPING","Orders can be cancelled before fulfillment begins. Once an order has shipped, the customer should follow the return process instead.");
      add("Shipping delays","SHIPPING","Standard delivery usually takes 3 to 5 business days. Delays beyond 7 business days should be escalated with the order number.");
      add("Subscription cancellation","GENERAL","Customers can cancel a subscription from Account settings. Cancellation stops the next renewal and access remains active through the paid period.");
    }
  }
  private void add(String title, String category, String content) {
    jdbc.update("INSERT INTO knowledge_documents(title,content,category) VALUES (?,?,?)", title, content, category);
  }
}