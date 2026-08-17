package com.jachwisunbae.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

import com.jachwisunbae.auth.controller.dto.TokenResponse;
import com.jachwisunbae.auth.token.RefreshTokenHasher;
import com.jachwisunbae.common.exception.BusinessException;
import com.jachwisunbae.common.exception.DomainErrorCode;

@Testcontainers
@SpringBootTest(properties = "spring.main.web-application-type=none")
class AuthServiceTest {

    @Container
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4");

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Autowired
    AuthService service;

    @Autowired
    RefreshTokenHasher hasher;

    @Autowired
    JdbcTemplate jdbcTemplate;

    Long memberId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM refresh_tokens");
        jdbcTemplate.update("DELETE FROM members");
        jdbcTemplate.update("""
                INSERT INTO members (subject, email, name, last_login_at)
                VALUES ('auth-subject', 'auth@example.com', '회원', CURRENT_TIMESTAMP(6))
                """);
        memberId = jdbcTemplate.queryForObject("SELECT id FROM members", Long.class);
    }

    @Test
    void rotatesRefreshTokenAndRevokesPreviousToken() {
        String currentToken = "current-refresh-token";
        insertToken(currentToken, LocalDateTime.now().plusDays(1), null);

        TokenResponse response = service.rotate(currentToken);

        assertThat(response.refreshToken()).isNotEqualTo(currentToken);
        assertThat(jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM refresh_tokens
                WHERE token_hash = ? AND revoked_at IS NOT NULL
                """, Long.class, hasher.hash(currentToken))).isOne();
        assertThat(jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM refresh_tokens
                WHERE token_hash = ? AND revoked_at IS NULL
                """, Long.class, hasher.hash(response.refreshToken()))).isOne();
    }

    @Test
    void reusedTokenRevokesAllActiveTokensForMember() {
        String reusedToken = "reused-refresh-token";
        insertToken(reusedToken, LocalDateTime.now().plusDays(1), LocalDateTime.now().minusMinutes(1));
        insertToken("other-active-token", LocalDateTime.now().plusDays(1), null);

        assertThatThrownBy(() -> service.rotate(reusedToken))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getCode())
                                .isEqualTo(DomainErrorCode.REFRESH_TOKEN_REUSED));
        assertThat(jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM refresh_tokens
                WHERE member_id = ? AND revoked_at IS NULL
                """, Long.class, memberId)).isZero();
    }

    @Test
    void logoutRevokesCurrentMembersToken() {
        String currentToken = "logout-refresh-token";
        insertToken(currentToken, LocalDateTime.now().plusDays(1), null);

        service.logout(memberId, currentToken);

        assertThat(jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM refresh_tokens
                WHERE token_hash = ? AND revoked_at IS NOT NULL
                """, Long.class, hasher.hash(currentToken))).isOne();
    }

    private void insertToken(String rawToken, LocalDateTime expiresAt, LocalDateTime revokedAt) {
        jdbcTemplate.update("""
                INSERT INTO refresh_tokens (member_id, token_hash, expires_at, revoked_at)
                VALUES (?, ?, ?, ?)
                """, memberId, hasher.hash(rawToken), expiresAt, revokedAt);
    }
}
