package com.ayush.support;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "app.demo-data.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.jwt.secret=unit-test-secret-that-is-long-enough-1234567890",
        "app.openai.api-key=",
        "app.openai.embedding-dimensions=1536"
})
class SupportPlatformApplicationTests {
    @Test
    void contextLoads() {}
}
