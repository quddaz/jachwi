package com.jachwisunbae.member.repository;

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

@Testcontainers
@SpringBootTest(properties = "spring.main.web-application-type=none")
class JdbcMemberRepositoryTest {

    @Container
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4");

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Autowired
    MemberRepository repository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM members");
    }

    @Test
    void findsMemberById() {
        LocalDateTime loginAt = LocalDateTime.of(2026, 8, 15, 20, 0);
        jdbcTemplate.update("""
                INSERT INTO members (subject, email, name, last_login_at)
                VALUES (?, ?, ?, ?)
                """, "google-subject", "user@example.com", "이자취", loginAt);
        Long memberId = jdbcTemplate.queryForObject(
                "SELECT id FROM members WHERE subject = ?",
                Long.class,
                "google-subject");

        assertThat(repository.findById(memberId))
                .hasValueSatisfying(member -> {
                    assertThat(member.getSubject()).isEqualTo("google-subject");
                    assertThat(member.getEmail()).isEqualTo("user@example.com");
                    assertThat(member.getName()).isEqualTo("이자취");
                    assertThat(member.getLastLoginAt()).isEqualTo(loginAt);
                });
    }

    @Test
    void returnsEmptyWhenMemberDoesNotExist() {
        assertThat(repository.findById(999L)).isEmpty();
    }
}
