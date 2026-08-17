package com.jachwisunbae.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import com.jachwisunbae.auth.token.RefreshToken;

@Testcontainers
@SpringBootTest(properties = "spring.main.web-application-type=none")
class JdbcRefreshTokenRepositoryTest {

    @Container
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4");

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Autowired
    RefreshTokenRepository repository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    Long memberId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM refresh_tokens");
        jdbcTemplate.update("DELETE FROM members");
        jdbcTemplate.update("""
                INSERT INTO members (subject, email, name, last_login_at)
                VALUES ('refresh-subject', 'refresh@example.com', '회원', '2026-08-17 00:00:00')
                """);
        memberId = jdbcTemplate.queryForObject("SELECT id FROM members", Long.class);
    }

    @Test
    void savesAndFindsRefreshTokenByHash() {
        LocalDateTime expiresAt = LocalDateTime.of(2026, 9, 1, 0, 0);
        RefreshToken saved = repository.save(RefreshToken.issue(memberId, "a".repeat(64), expiresAt));

        assertThat(repository.findByHashForUpdate("a".repeat(64)))
                .hasValueSatisfying(token -> {
                    assertThat(token.getId()).isEqualTo(saved.getId());
                    assertThat(token.getMemberId()).isEqualTo(memberId);
                    assertThat(token.getRevokedAt()).isNull();
                });
    }

    @Test
    void revokesEveryActiveTokenForMember() {
        LocalDateTime expiresAt = LocalDateTime.of(2026, 9, 1, 0, 0);
        repository.save(RefreshToken.issue(memberId, "a".repeat(64), expiresAt));
        repository.save(RefreshToken.issue(memberId, "b".repeat(64), expiresAt));

        repository.revokeAllActiveByMemberId(memberId, LocalDateTime.of(2026, 8, 17, 1, 0));

        assertThat(jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM refresh_tokens
                WHERE member_id = ? AND revoked_at IS NULL
                """, Long.class, memberId)).isZero();
    }
}
