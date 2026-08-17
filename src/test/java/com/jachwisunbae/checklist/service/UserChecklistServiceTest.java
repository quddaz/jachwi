package com.jachwisunbae.checklist.service;

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

import com.jachwisunbae.checklist.service.dto.CreateUserChecklistCommand;
import com.jachwisunbae.checklist.service.dto.UpdateUserChecklistCommand;
import com.jachwisunbae.checklist.service.dto.UserChecklistDetailResult;
import com.jachwisunbae.checklist.type.Stage;

@Testcontainers
@SpringBootTest(properties = "spring.main.web-application-type=none")
class UserChecklistServiceTest {

    @Container
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4");

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Autowired
    UserChecklistService service;

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
                       ('ON_SITE', 'OPTIONAL', '선택 질문', '선택 안내')
                """);
        List<Long> itemIds = jdbcTemplate.queryForList(
                "SELECT id FROM system_check_items ORDER BY id", Long.class);
        coreItemId = itemIds.get(0);
        optionalItemId = itemIds.get(1);
    }

    @Test
    void createPersistsChecklistAndAutomaticallyIncludesCoreItem() {
        UserChecklistDetailResult result = service.create(
                memberId,
                new CreateUserChecklistCommand(
                        "  내 체크  ", Stage.ON_SITE, List.of(optionalItemId)));

        assertThat(result.name()).isEqualTo("내 체크");
        assertThat(result.items())
                .extracting(item -> item.checkItemId())
                .containsExactly(coreItemId, optionalItemId);
    }

    @Test
    void updateAllowsRemovingAutomaticallyIncludedCoreItem() {
        UserChecklistDetailResult created = service.create(
                memberId,
                new CreateUserChecklistCommand(
                        "기존 체크", Stage.ON_SITE, List.of(optionalItemId)));

        UserChecklistDetailResult updated = service.update(
                memberId,
                created.checklistId(),
                new UpdateUserChecklistCommand(
                        "변경 체크", List.of(optionalItemId)));

        assertThat(updated.name()).isEqualTo("변경 체크");
        assertThat(updated.items())
                .extracting(item -> item.checkItemId())
                .containsExactly(optionalItemId);
    }

    @Test
    void deleteExcludesChecklistFromMemberList() {
        UserChecklistDetailResult created = service.create(
                memberId,
                new CreateUserChecklistCommand(
                        "삭제 체크", Stage.ON_SITE, List.of(optionalItemId)));

        service.delete(memberId, created.checklistId());

        assertThat(service.findAll(memberId, null)).isEmpty();
    }
}
