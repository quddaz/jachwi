package com.jachwisunbae.checklist.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

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

import com.jachwisunbae.checklist.entity.UserChecklist;
import com.jachwisunbae.checklist.entity.UserChecklistItem;
import com.jachwisunbae.checklist.type.Stage;

@Testcontainers
@SpringBootTest(properties = "spring.main.web-application-type=none")
class JdbcUserChecklistRepositoryTest {

    @Container
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4");

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Autowired
    JdbcUserChecklistRepository repository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    Long memberId;
    Long coreItemId;
    Long optionalItemId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM user_checklist_items");
        jdbcTemplate.update("DELETE FROM user_checklists");
        jdbcTemplate.update("DELETE FROM system_check_items");
        jdbcTemplate.update("DELETE FROM members");
        jdbcTemplate.update("""
                INSERT INTO members (subject, email, name, last_login_at)
                VALUES ('subject', 'user@example.com', '회원', ?)
                """, LocalDateTime.of(2026, 8, 15, 20, 0));
        memberId = jdbcTemplate.queryForObject("SELECT id FROM members", Long.class);
        jdbcTemplate.update("""
                INSERT INTO system_check_items (stage, item_type, question, guide)
                VALUES ('ON_SITE', 'CORE', '핵심 질문', NULL),
                       ('ON_SITE', 'OPTIONAL', '선택 질문', '안내')
                """);
        List<Long> ids = jdbcTemplate.queryForList(
                "SELECT id FROM system_check_items ORDER BY id", Long.class);
        coreItemId = ids.get(0);
        optionalItemId = ids.get(1);
    }

    @Test
    void savesFindsReplacesAndSoftDeletesChecklist() {
        UserChecklist saved = repository.save(
                UserChecklist.create(memberId, "현장 체크", Stage.ON_SITE));

        repository.replaceItems(saved.getId(), List.of(coreItemId, optionalItemId));

        assertThat(repository.findActiveByIdAndMemberId(saved.getId(), memberId)).isPresent();
        assertThat(repository.findItems(saved.getId()))
                .extracting(UserChecklistItem::getSystemCheckItemId)
                .containsExactly(coreItemId, optionalItemId);

        saved.rename("변경 체크");
        repository.save(saved);
        assertThat(repository.findAllActiveByMemberId(memberId, Stage.ON_SITE))
                .singleElement()
                .extracting(UserChecklist::getName)
                .isEqualTo("변경 체크");

        repository.softDelete(saved.getId());
        assertThat(repository.findActiveByIdAndMemberId(saved.getId(), memberId)).isEmpty();
    }
}
