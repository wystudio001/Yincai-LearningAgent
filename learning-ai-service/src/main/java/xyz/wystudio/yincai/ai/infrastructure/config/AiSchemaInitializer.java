package xyz.wystudio.yincai.ai.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS learning_ai.ai_model_provider (
                  id BIGINT NOT NULL PRIMARY KEY,
                  tenant_id BIGINT DEFAULT 0,
                  name VARCHAR(100) NOT NULL,
                  provider_code VARCHAR(64) NOT NULL,
                  base_url VARCHAR(512) NOT NULL,
                  api_key VARCHAR(1024) NOT NULL,
                  default_model VARCHAR(128) NOT NULL,
                  enabled TINYINT(1) NOT NULL DEFAULT 0,
                  timeout_ms INT NOT NULL DEFAULT 120000,
                  temperature DECIMAL(4,2) NOT NULL DEFAULT 0.20,
                  max_tokens INT NOT NULL DEFAULT 2048,
                  remark VARCHAR(500) NULL,
                  deleted TINYINT(1) NOT NULL DEFAULT 0,
                  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                  KEY idx_ai_model_provider_enabled (enabled, deleted),
                  KEY idx_ai_model_provider_provider_code (provider_code, deleted)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);
    }
}
