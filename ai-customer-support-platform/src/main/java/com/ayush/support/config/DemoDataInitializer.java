package com.ayush.support.config;

import com.ayush.support.domain.KnowledgeDocument;
import com.ayush.support.domain.Role;
import com.ayush.support.domain.User;
import com.ayush.support.repository.KnowledgeDocumentRepository;
import com.ayush.support.repository.UserRepository;
import com.ayush.support.ai.EmbeddingService;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DemoDataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final KnowledgeDocumentRepository knowledgeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmbeddingService embeddingService;
    private final boolean enabled;

    public DemoDataInitializer(UserRepository userRepository,
                               KnowledgeDocumentRepository knowledgeRepository,
                               PasswordEncoder passwordEncoder,
                               EmbeddingService embeddingService,
                               @Value("${app.demo-data.enabled}") boolean enabled) {
        this.userRepository = userRepository;
        this.knowledgeRepository = knowledgeRepository;
        this.passwordEncoder = passwordEncoder;
        this.embeddingService = embeddingService;
        this.enabled = enabled;
    }

    @Override
    public void run(String... args) {
        if (!enabled) return;
        seedUser("admin@example.com", "Admin@12345", "Demo Admin", Role.ADMIN);
        seedUser("agent@example.com", "Agent@12345", "Demo Agent", Role.AGENT);
        seedUser("customer@example.com", "Customer@12345", "Demo Customer", Role.CUSTOMER);

        if (knowledgeRepository.count() == 0) {
            List<String[]> docs = List.of(
                    new String[]{"Refund policy", "Refund requests can be submitted within 30 days of purchase. Approved refunds are returned to the original payment method.", "refund-policy.md"},
                    new String[]{"Password reset", "Customers can reset a password from the sign-in page using the Forgot password link. If email delivery fails, an agent should verify the account email.", "account-help.md"},
                    new String[]{"Shipping delays", "Standard shipping normally takes 3 to 7 business days. During carrier disruptions, agents should verify the latest carrier status before promising a delivery date.", "shipping.md"},
                    new String[]{"Billing disputes", "For duplicate or unexpected charges, verify the order and payment reference before issuing a correction. Escalate unresolved payment disputes to the billing team.", "billing.md"},
                    new String[]{"Technical troubleshooting", "For login or product errors, first collect the error message, affected device or browser, and approximate time of occurrence. Ask the customer to retry after clearing cache when appropriate.", "technical-troubleshooting.md"}
            );
            for (String[] d : docs) {
                KnowledgeDocument doc = new KnowledgeDocument();
                doc.setTitle(d[0]);
                doc.setContent(d[1]);
                doc.setSource(d[2]);
                knowledgeRepository.save(doc);
            }
            if (hasOpenAiKey()) knowledgeRepository.findAll().forEach(embeddingService::index);
        }
    }

    private void seedUser(String email, String password, String name, Role role) {
        if (userRepository.existsByEmailIgnoreCase(email)) return;
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(name);
        user.setRole(role);
        userRepository.save(user);
    }

    private boolean hasOpenAiKey() {
        return System.getenv("OPENAI_API_KEY") != null && !System.getenv("OPENAI_API_KEY").isBlank();
    }
}
