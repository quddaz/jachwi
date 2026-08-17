package com.jachwisunbae.member.service;

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
class MemberServiceTest {

    @Container
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4");

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Autowired
    MemberService service;

    @Autowired
    JdbcTemplate jdbcTemplate;

    Long memberId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM members");
        jdbcTemplate.update("""
                INSERT INTO members (subject, email, name, last_login_at)
                VALUES (?, ?, ?, ?)
                """, "member-subject", "member@example.com", "이회원",
                LocalDateTime.of(2026, 8, 17, 0, 0));
        memberId = jdbcTemplate.queryForObject("SELECT id FROM members", Long.class);
    }

    @Test
    void returnsCurrentMemberProfile() {
        var result = service.getMe(memberId);

        assertThat(result.memberId()).isEqualTo(memberId);
        assertThat(result.name()).isEqualTo("이회원");
        assertThat(result.email()).isEqualTo("member@example.com");
    }
}
