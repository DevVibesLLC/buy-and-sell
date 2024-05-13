package am.devvibes.buyandsell.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

// Defines beans or customizations for a test.
@TestConfiguration
// Enables JPA auditing. auditorAwareRef value must match with the name of AuditorAware bean.
@EnableJpaAuditing(auditorAwareRef = "testAuditProvider")
public class TestAuditingConfig {

    public static final String TEST_AUDITOR = "Test Auditor";

    @Bean
    @Primary
    public AuditorAware<String> testAuditProvider() {
        return () -> Optional.of(TEST_AUDITOR);
    }
}